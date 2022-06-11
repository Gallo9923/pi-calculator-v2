import Pi.PiControllerPrx;
import Pi.WorkerPrx;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;

public class Worker{

    public static void main(String[] args) {

        int status = 0;
        java.util.List<String> extraArgs = new java.util.ArrayList<String>();

        try(Communicator communicator = com.zeroc.Ice.Util.initialize(args, "config.worker", extraArgs))
        {
            if(!extraArgs.isEmpty())
            {
                System.err.println("too many arguments");
                status = 1;
            }
            else
            {
                PiControllerPrx piControllerPrx = null;
                do {
                    piControllerPrx = getPiControllerPrx(communicator);
                }while(piControllerPrx == null);

                com.zeroc.Ice.ObjectAdapter adapter = communicator.createObjectAdapter("PiWorker");
                com.zeroc.Ice.Properties properties = communicator.getProperties();
                com.zeroc.Ice.Identity id = com.zeroc.Ice.Util.stringToIdentity(properties.getProperty("Identity"));
                ObjectPrx subscriber = adapter.add(new WorkerI(piControllerPrx), id);

                adapter.activate();


                final WorkerPrx subscriberProxy = WorkerPrx.uncheckedCast(communicator.stringToProxy(properties.getProperty("MyProxy")).ice_twoway());
                piControllerPrx.subscribe(subscriberProxy);
                System.out.println("Subscribed - " + subscriberProxy.toString());

                final PiControllerPrx piControllerPrxF = piControllerPrx;
                final WorkerPrx workerPrxF = subscriberProxy;

                Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                    public void run() {
                        try {
                            piControllerPrxF.unsubscribe(workerPrxF);

                            System.out.println("Unsubscribed - " + workerPrxF.toString());
                            communicator.destroy();
                        } catch (Exception e) {
                            // Do nothing
                        }
                    }
                }, "Shutdown-thread"));

                communicator.waitForShutdown();
            }
        }

        System.exit(status);
    }

    private static PiControllerPrx getPiControllerPrx(Communicator communicator){
        PiControllerPrx piControllerPrx = null;
        try {
            piControllerPrx = PiControllerPrx.checkedCast(communicator.stringToProxy("PiController")).ice_locatorCacheTimeout(0);
        }
        catch(com.zeroc.Ice.NotRegisteredException ex)
        {
            com.zeroc.IceGrid.QueryPrx query = com.zeroc.IceGrid.QueryPrx.checkedCast(communicator.stringToProxy("PiIceGrid/Query"));
            piControllerPrx = PiControllerPrx.checkedCast(query.findObjectByType("::Pi::PiController"));
        }
        if(piControllerPrx == null)
        {
            System.err.println("couldn't find a `::Pi::PiController' object");
        }
        return piControllerPrx;
    }

}