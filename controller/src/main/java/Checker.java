import Enums.TaskState;
import Pi.PiController;
import Pi.Task;


import java.util.Random;
import java.util.concurrent.Semaphore;

public class Checker implements Runnable {

    private PiControllerI piController;
    private long millisTimeout;
    private String taskId;
    private Semaphore jobSem;

    public Checker(PiControllerI piController, long millisTimeout, String taskId, Semaphore jobSem){
        this.piController = piController;
        this.millisTimeout = millisTimeout;
        this.taskId = taskId;
        this.jobSem = jobSem;
    }

    @Override
    public void run() {
        boolean pending = true;
        while(pending){
            try {
                Thread.sleep(millisTimeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {

                try{
                    this.jobSem.acquire();
                    Task task = this.piController.tasks.get(this.taskId);

                    if (task.state.equals(TaskState.IN_PROGRESS.toString())){
                        task.state = TaskState.PENDING.toString();
                        piController.pendingTasks.add(task);
                        piController.notifyAllSubscribers(task.jobId);
                    }else if (task.state.equals(TaskState.DONE.toString())){
                        pending = false;
                        break;
                    }
                }catch (InterruptedException e){
                }
            }
        }
    }
}
