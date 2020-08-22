package retry;


import java.util.concurrent.Callable;
import java.util.logging.Logger;

/**
 * Design aimed to perform a function in a given number of attempts.
 * @param <T> return type of the function
 */
public class Retryer<T> {

    private static final Logger LOG = Logger.getLogger(Retryer.class.getName());

    private int attempts;

    public Retryer(final int retryCount) {

        this.attempts = retryCount;
    }

    public T run(final Callable<T> function) throws Exception {

        try {
            return function.call();
        }
        catch (Exception e) {
            return runAgain(function);
        }
    }

    private T runAgain(Callable<T> function) throws Exception {

        Exception ex = null;
        int retryCount = 2;

        while (retryCount < attempts) {

            try {
                LOG.info(String.format("Tentando executar a chamada novamente, tentativa %s", retryCount));
                return function.call();
            }
            catch (Exception e) {

                retryCount++;

                if (retryCount >= attempts) {
                    ex = e;
                    LOG.info(String.format("A execucao da chamada nao foi bem sucedida apos %s tentativas", attempts));
                    break;
                }
            }
        }
        throw ex;
    }
}
