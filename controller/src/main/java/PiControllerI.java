import Enums.TaskState;
import Pi.*;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Current;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Properties;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.temporal.ChronoUnit;
import java.util.*;
import Pi.WorkerPrx;

import java.time.LocalDateTime;
import java.util.concurrent.Semaphore;

public class PiControllerI implements PiController {

    private Communicator communicator;
    private HashMap<String, Job> jobs;
    private Semaphore jobSem;
    private Semaphore observerSem;

    protected ArrayList<Task> pendingTasks;

    protected HashMap<String, Task> tasks;
    private ArrayList<WorkerPrx> workers;

    private HashMap<String, Boolean> jobState;

    public PiControllerI(Communicator communicator){
        this.communicator = communicator;
        this.jobs = new HashMap<String, Job>();
        this.workers = new ArrayList<>();
        this.tasks = new HashMap<>();
        this.pendingTasks = new ArrayList<>();

        this.jobSem = new Semaphore(1, true);
        this.observerSem = new Semaphore(1, true);

        this.jobState = new HashMap<String, Boolean>();
    }

    @Override
    public void calculatePi(PiRequest request, ClientPrx clientProxy, Current current) {
        new Thread(() -> calculatePi(request, clientProxy)).start();
    }

    @Override
    public void setTaskResult(TaskResult taskResult, Current current) {
        System.out.println("setTaskResult " + taskResult.taskId);
        new Thread(() -> setTaskResult(taskResult)).start();
    }

    public void calculatePi(PiRequest request, ClientPrx clientProxy) {
        System.out.println("calculatePi" + clientProxy.toString());

        Properties p = communicator.getProperties();

        int jobBatchSize;

        if (request.nPower > 6) {
            jobBatchSize = Integer.parseInt(p.getProperty("BATCH_SIZE1"));
        } else {
            jobBatchSize = Integer.parseInt(p.getProperty("BATCH_SIZE2"));
        }

        String jobId = UUID.randomUUID().toString();
        Job job = new Job(jobId, request.nPower, request.seed, 0.0, request.epsilonPower, LocalDateTime.now().toString(), LocalDateTime.now().toString(), "0", "0", clientProxy.toString(), jobBatchSize, "0");
        jobs.put(jobId, job);
        jobState.put(jobId, Boolean.FALSE);

        new Thread(() -> notifyAllSubscribers(jobId)).start();
    }



    protected void notifyAllSubscribers(String jobId){
        
        System.out.println("Notify all subscribers for jobId " + jobId);
        
        try {
            this.observerSem.acquire();

            ArrayList<WorkerPrx> observersToRemove = new ArrayList<WorkerPrx>();
            for (WorkerPrx worker : this.workers){
                try {
                    worker.notifyTaskAvailable(jobId);
                    // TODO: Find the correct exception to catch
                }catch(Exception e){
                    observersToRemove.add(worker);
                }
            }

            for (WorkerPrx worker : observersToRemove){
                unsubscribe(worker, null);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally{
            this.observerSem.release();
        }
    }

    @Override
    public Task getTask(String jobId, Current current) {
        System.out.println("getTask " + jobId);
        Task task = null;
        try {
            this.jobSem.acquire();
            task = getTask(jobId);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally{
            this.jobSem.release();
            return task;
        }


    }

    public Task getTask(String jobId) {

        System.out.println("Get task for jobId " + jobId);

        Task task = null;
        if (pendingTasks.size() > 0){
            System.out.println("Pending tasks");
            task = pendingTasks.get(0);
            task.state =  TaskState.IN_PROGRESS.toString();

        }else{
            System.out.println("No pending tasks");

            Job job = jobs.get(jobId);

            BigInteger taskCounter = new BigInteger(job.taskCounter);
            BigInteger n = new BigInteger("10").pow(job.nPower);
            BigInteger batchSize = new BigInteger(job.batchSize + "");

            // TODO: Start Transaction
            boolean taskNeeded = taskCounter.compareTo(n.divide(batchSize)) == -1;
            System.out.println("task counter: " + taskCounter + " - " + taskNeeded);
            if (taskNeeded){
                System.out.println("New task");

                taskCounter = new BigInteger(job.taskCounter).add(BigInteger.ONE);
                job.taskCounter = taskCounter.toString();

                //LocalDateTime createDate = LocalDateTime.now();
                //String taskId = taskTable.create(jobId, job.seed + "", job.batchSize + "", createDate.toString(), TaskState.IN_PROGRESS + "", taskCounter.toString(), "0", job.epsilonPower + "");

                String taskId = UUID.randomUUID().toString();
                task = new Task(taskId, job.id, job.seed, job.batchSize, LocalDateTime.now().toString(), TaskState.IN_PROGRESS.toString(), taskCounter.toString(), 0, job.epsilonPower);
                System.out.println("task created");

                this.tasks.put(taskId, task);
                System.out.println("task size: " + this.tasks.size());

                long taskMillisTimeout = Long.parseLong(communicator.getProperties().getProperty("taskMillisTimeout"));
                new Thread(new Checker(this, taskMillisTimeout, task.id, jobSem)).start();

            }else if (true || jobState.get(jobId).equals(Boolean.FALSE) ){
                System.out.println("Calculating pi");

                jobState.put(jobId, Boolean.TRUE);

                // Calculate PI
                BigDecimal pi = (new BigDecimal(job.pointsInside).divide(new BigDecimal(n))).multiply(new BigDecimal(4));
                job.pi = pi.toString();

                LocalDateTime startTime = LocalDateTime.parse(job.startDate);
                LocalDateTime finishTime = LocalDateTime.now();

                long milliseconds = ChronoUnit.MILLIS.between(startTime, finishTime);
                long seconds = ChronoUnit.SECONDS.between(startTime, finishTime);
                long minutes = ChronoUnit.MINUTES.between(startTime, finishTime);

                System.out.println("Updating Job's Pi Result " + pi.toString() + " / milli: " + milliseconds + " / seconds: " + seconds + " / minutes: " + minutes);

                Time time = new Time(milliseconds + "", seconds + "", minutes + "" );

                Result result = new Result(pi.toString(), job.repNumbers, time);

                Pi.ClientPrx clientProxy = Pi.ClientPrx.checkedCast(communicator.stringToProxy(job.clientProxy)).ice_twoway().ice_secure(false);
                clientProxy.setResult(result);
            }
            // TODO: END Transaction
        }
        System.out.println("task returned");
        return task;
    }

    public void setTaskResult(TaskResult taskResult) {
        try {
            // Step 1: Acquire del semaphore
            this.jobSem.acquire();

            String taskId = taskResult.taskId;
            Task task = this.tasks.get(taskId);

            task.state = TaskState.DONE.toString();
            task.result = taskResult.pointsInside;

            Job job = this.jobs.get(task.jobId);

            BigInteger pointsInside = new BigInteger(job.pointsInside);
            BigInteger workerResult = new BigInteger(taskResult.pointsInside + "");
            pointsInside = pointsInside.add(workerResult);
            job.pointsInside = pointsInside.toString();

            System.out.println("set task result called");

            notifyAllSubscribers(job.id);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally{
            this.jobSem.release();
        }
    }

    @Override
    public void notifyPiResult(Job job, Current current) {
        System.out.println("notifyPiResult " + job.id);
    }

    @Override
    public void subscribe(WorkerPrx workerProxy, Current current) {
        try {
            this.observerSem.acquire();
            if (!this.workers.contains(workerProxy)){
                this.workers.add(workerProxy);

                System.out.println("ATTACHED: " + workerProxy.toString());
            }else{
                System.out.println("ALREADY ATTACHED: " + workerProxy.toString());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally{
            this.observerSem.release();
        }
    }

    @Override
    public void unsubscribe(WorkerPrx workerProxy, Current current) {
        try {
            this.observerSem.acquire();
            boolean removed = this.workers.remove(workerProxy);
            if (removed){
                System.out.println("DETACHED: " + workerProxy.toString());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally{
            this.observerSem.release();
        }
    }

}
