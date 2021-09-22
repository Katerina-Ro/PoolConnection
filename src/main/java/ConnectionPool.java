import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ConnectionPool {
    //Данные для подключения к БД
    private String url;
    private String username;
    private String password;

    //Данные пула соединений: максимально возможное количество соединений в данном пуле
    private final int minPoolSize;
    private int maxPoolSize;

    //Данные для организации connection pool
    private BlockingQueue<Connection> freePool;
    private BlockingQueue<Connection> occopuredPool;
    Connection connection = null;

    public ConnectionPool(String url, String username, String password, int minPoolSize) throws SQLException {
        this.url = url;
        this.username = username;
        this.password = password;
        this.minPoolSize = minPoolSize;

        freePool = new ArrayBlockingQueue<>(minPoolSize);
        occopuredPool = new ArrayBlockingQueue<>(minPoolSize);

        //заполняем пул соединений открытыми подключениями
        for (int i = 0; i < minPoolSize; i++) {
            freePool.offer(DriverManager.getConnection(url, username, password));
        }
    }

    public void setMaxPoolSize(int maxPoolSize) {this.maxPoolSize = maxPoolSize;}

    //Получаем connection из пула соединений
    public synchronized Connection getConnection() throws SQLException, InterruptedException {
        //если нет свободных соединений, то увеличиваем pool connection
        if (occopuredPool.size() == maxPoolSize) {
            System.out.println("Нет свободных соединений. Расширяем очередь");
            setMaxPoolSize(maxPoolSize = minPoolSize * 2);
            freePool = new ArrayBlockingQueue<>(maxPoolSize);
            occopuredPool =  new ArrayBlockingQueue<>(maxPoolSize);
            //заполняем пул соединений открытыми соединениями
            for (int i = 0; i <= maxPoolSize; i++) {
                freePool.offer(DriverManager.getConnection(url, username, password));
            }
        }
        connection = getConnectionFromPool();
        /*
        Если в течение времени не выполняется никаких действий в БД, то соединение с БД теряется.
        По этой причине, нужно убедиться, что соединение активно, и, если нужно, подключить его заново
        */
        connection = makeAvailable(connection);
        return connection;
    }

    private Connection getConnectionFromPool() throws InterruptedException {
            connection = freePool.take(); //возвращает из начала очереди элемент и удаляет его из очереди
            occopuredPool.offer(connection); // добавляем новое действующее соединение в очередь из занятых соединений
        return connection;
    }

    private Connection makeAvailable (Connection connection) throws SQLException{
        if (isConnectionAvailable(connection)){
            return connection;
        }
        //если соединение недоступно, то удаляем из очереди занятых подключений неактивные соединения
        occopuredPool.remove(connection);
        connection.close();

        //после очистки очереди занятых соединений создаем новое подключение, добавляем в очередь, возвращаем соединение
        connection = DriverManager.getConnection(url, username, password);
        occopuredPool.offer(connection);
        return connection;
    }

    //запускаем SQL, чтобы проверить доступно ли соединение
    private boolean isConnectionAvailable(Connection connection) {
        try (Statement statement = connection.createStatement()){
            statement.executeQuery("SQL-запрос");
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    //Возвращаем connection в pool и одновременни уменьшаем пул соединений до минимального значения
    public synchronized void returnConnection(Connection connection) throws InterruptedException {
        if (connection != null) {
            freePool.offer(occopuredPool.take());
            }
        BlockingQueue<Connection> freePool2 = new ArrayBlockingQueue<>(minPoolSize);
        for (int i = 0; i < minPoolSize; i++) {
            freePool2.offer(freePool.take());}
        freePool = freePool2;
    }
}

