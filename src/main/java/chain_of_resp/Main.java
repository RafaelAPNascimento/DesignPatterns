package chain_of_resp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;

public class Main
{
    static Logger LOG = Logger.getLogger(Main.class.getSimpleName());

    static BufferedReader reader;
    static Server server;

    public static void main(String[] args) throws IOException
    {
        init();

        boolean success = true;
        do
        {
            System.out.println("Enter email:");
            String email = reader.readLine();
            System.out.println("Input Password:");
            String password = reader.readLine();
            success = server.logIn(email, password);
        }
        while (!success);
    }

    static void init()
    {
        reader = new BufferedReader(new InputStreamReader(System.in));
        server = new Server();

        server.register("admin@example.com", "admin_pass");
        server.register("user@example.com", "user_pass");

        Middleware middleware = new ThrottlingMiddleware(2);
        middleware.linkWith(new UserExistsMiddleware(server))
                    .linkWith(new RoleCheckMiddleware());

        server.setMiddleware(middleware);
    }

    static abstract class Middleware
    {
        protected Middleware next;

        public abstract boolean check(String email, String password);

        public Middleware linkWith(Middleware next)
        {
            this.next = next;
            return next;
        }

        public boolean checkNext(String email, String password)
        {
            if (next == null)
                return true;
            return next.check(email, password);
        }
    }

    static class ThrottlingMiddleware extends Middleware
    {
        private int requestPerMinue;
        private int request;
        private long currentTime;

        public ThrottlingMiddleware(int requestPerMinue)
        {
            this.requestPerMinue = requestPerMinue;
            this.currentTime = System.currentTimeMillis();
        }

        @Override
        public boolean check(String email, String password)
        {
            if (System.currentTimeMillis() > currentTime + 60_000)
            {
                request = 0;
                currentTime = System.currentTimeMillis();
            }
            request++;

            if (request > requestPerMinue)
            {
                System.out.println("Request time limit exceeded!");
                Thread.currentThread().stop();
            }
            LOG.log(INFO, String.format("%s OK", getClass().getSimpleName()));
            return checkNext(email, password);
        }
    }

    static class UserExistsMiddleware extends Middleware
    {
        private Server server;

        public UserExistsMiddleware(Server server)
        {
            this.server = server;
        }

        @Override
        public boolean check(String email, String password)
        {
            if (!server.hasEmail(email))
            {
                System.out.println("This email is not registered!");
                return false;
            }
            if (!server.isValidPassword(email, password))
            {
                System.out.println("Wrong password!");
                return false;
            }
            LOG.log(INFO, String.format("%s OK", getClass().getSimpleName()));
            return checkNext(email, password);
        }
    }

    static class RoleCheckMiddleware extends Middleware
    {
        @Override
        public boolean check(String email, String password)
        {
            if (email.equals("admin@example.com"))
            {
                System.out.println("Hello, admin");
                return true;
            }
            LOG.log(INFO, String.format("%s OK", getClass().getSimpleName()));
            return checkNext(email, password);
        }
    }

    static class Server
    {
        private Middleware middleware;
        private HashMap<String, String> users = new HashMap<>();

        public void setMiddleware(Middleware middleware)
        {
            this.middleware = middleware;
        }

        public boolean logIn(String email, String password)
        {
            if (middleware.check(email, password))
            {
                System.out.println("Authorization has been successsful!");

                // do more something usefull...
                return true;
            }
            return false;
        }

        public boolean hasEmail(String email)
        {
            return users.containsKey(email);
        }

        public boolean isValidPassword(String email, String password)
        {
            return users.get(email).equals(password);
        }

        public void register(String email, String password)
        {
            users.put(email, password);
        }
    }
}
