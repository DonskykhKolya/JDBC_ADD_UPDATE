package ua.kiev.prog.case2;

import ua.kiev.prog.shared.Client;
import ua.kiev.prog.shared.ConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Main {
    static final String DB_CONNECTION = "jdbc:mysql://localhost:3306/mydb";
    static final String DB_USER = "root";
    static final String DB_PASSWORD = "ninetyonenikolya";

    public static void main(String[] args) throws SQLException {
        Scanner sc = new Scanner(System.in);

        ConnectionFactory factory = new ConnectionFactory(
                DB_CONNECTION, DB_USER, DB_PASSWORD);

        Connection conn = factory.getConnection();
        try {
            ClientDAOEx dao = new ClientDAOEx(conn, "clients");

            Client c = new Client("test", 1);
            Client cl = new Client("test2", 2);
            dao.add(c);
            dao.add(cl);

            List<Client> list = dao.getAll(Client.class);
            for (Client cli : list)
                System.out.println(cli);

            Client searchCl = dao.search(3, Client.class);
            System.out.println(searchCl + " was founded");
            searchCl.setName("emaN");
            searchCl.setAge(13);
            dao.update(searchCl);
            System.out.println(searchCl + "was updated");

            dao.delete(list.get(0));
        } finally {
            sc.close();
            if (conn != null) conn.close();
        }
    }
}
