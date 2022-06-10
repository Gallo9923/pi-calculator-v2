import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import Pi.ClientPrx;
import Pi.PiControllerPrx;
import Pi.PiRequest;

public class Client
{

    private static int epsilon;
    private static long seed;
    private static int n;

    public static void main(String[] args)
    {
        int status = 0;
        java.util.List<String> extraArgs = new java.util.ArrayList<>();

        try(com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args,"config.client",extraArgs))
        {
            if(!extraArgs.isEmpty())
            {
                System.err.println("too many arguments");
                status = 1;
                return;
            }

            com.zeroc.Ice.ObjectAdapter adapter = communicator.createObjectAdapter("Client");
            adapter.add(new ClientI(), com.zeroc.Ice.Util.stringToIdentity("client"));
            adapter.activate();

            ClientPrx clientPrx =  Pi.ClientPrx.uncheckedCast(adapter.createProxy(com.zeroc.Ice.Util.stringToIdentity("client")));
            if(clientPrx == null)
            {
                throw new Error("Invalid Callback proxy");
            }
            status = run(communicator, clientPrx);
        }

        System.exit(status);
    }

    private static int run(com.zeroc.Ice.Communicator communicator, ClientPrx clientPrx)
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        boolean exit = false;

        while(!exit) {
            System.out.println("------------- WELCOME TO PI CALCULATOR -------------\n"
                    + "1. SET SEED FOR RANDOM NUMBER GENERATOR.\n"
                    + "2. SET POWER OF 10 FOR NUMBERS TO GENERATE.\n"
                    + "3. SET EPSILON TO CALCULATE REPEATED NUMBERS.\n"
                    + "4. CALCULATE PI.");

            try {
                int sel = Integer.valueOf(reader.readLine());

                switch (sel) {
                    case 1:
                        System.out.print("SEED: ");
                        seed = Long.parseLong(reader.readLine());
                        break;
                    case 2:
                        System.out.print("10^N POWER: ");
                        n = Math.abs(Integer.valueOf(reader.readLine()));
                        break;
                    case 3:
                        System.out.print("EPSILON: ");
                        epsilon = Math.abs(Integer.valueOf(reader.readLine()));
                        break;
                    case 4:
                        System.out.println("Calculating PI...");
                        PiControllerPrx piControllerPrx = null;
                        do {
                            piControllerPrx = getPiControllerPrx(communicator);
                        }while(piControllerPrx == null);

                        Client.calculatePi(piControllerPrx, clientPrx);
                        break;
                    default:
                        System.out.println("Unknown option :(");
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return 0;
    }

    private static void calculatePi(PiControllerPrx piControllerPrx, ClientPrx clientPrx){

        if (Client.epsilon <= 0){
            System.out.println("Epsilon was not provided");
            return;
        }
        if (Client.seed == -1){
            System.out.println("The seed was not provided");
            return;
        }
        if (Client.n <= 0){
            System.out.println("N was not provided");
            return;
        }
        PiRequest piRequest = new PiRequest((short) Client.n, Client.seed, (short)Client.epsilon);
        piControllerPrx.calculatePi(piRequest, clientPrx);

    }

    private static PiControllerPrx getPiControllerPrx(com.zeroc.Ice.Communicator communicator){
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