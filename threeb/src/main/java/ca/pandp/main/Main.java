package ca.pandp.main;

import ca.pandp.plugins.consumers.Consumable;
import ca.pandp.plugins.producers.Producible;
import ca.pandp.shared.domainobjects.BeanModel;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * Created by Micha "Micha Did It" Pringle on Jun 07, 2015.
 * <p>
 * Load all the producers, execute them, and feed the results to all the consumers.
 */

@net.jcip.annotations.ThreadSafe
@net.jcip.annotations.Immutable
public class Main {
    private static final ProducerService PRODUCER_SERVICE_SINGLETON = new ProducerService();
    private static final ConsumerService CONSUMER_SERVICE_SINGLETON = new ConsumerService();

    /**
     * Run the application, linking all producers and consumers.
     *
     * @param args - ignored.
     */
    public static void main(String[] args) {
        try {
            final List<BeanModel> beans = PRODUCER_SERVICE_SINGLETON.getBeanModelList();
            CONSUMER_SERVICE_SINGLETON.generateCode(beans);
        } catch( ServiceConfigurationError error ) {
            System.out.println( "Holy shit, something went totally wrong." );
            error.printStackTrace();
        }
    }

    @net.jcip.annotations.ThreadSafe
    @net.jcip.annotations.Immutable
    private static class ProducerService {

        private final ServiceLoader<Producible> loader;

        private ProducerService() {
            loader = ServiceLoader.load(Producible.class);
        }

        public List<BeanModel> getBeanModelList( /* annotation? */) throws ServiceConfigurationError {
            List<BeanModel> beans = Lists.newArrayList();

            for (final Producible p : loader) {
                beans.add(p.produce( /* annotation? */));
            }
            return ImmutableList.copyOf(beans);
        }
    }

    @net.jcip.annotations.ThreadSafe
    @net.jcip.annotations.Immutable
    private static class ConsumerService {

        private final ServiceLoader<Consumable> loader;

        private ConsumerService() {
            loader = ServiceLoader.load(Consumable.class);
        }

        public void generateCode( final List<BeanModel> beans  /* , annotation? */ ) throws ServiceConfigurationError {
            for (final Consumable c : loader) {
                for (final BeanModel b : beans) {
                    c.consume(b  /* , annotation? */);
                }
            }
        }
    }
}
