
Introduction:

This project is an implementation of a work-stealing scheduler, its testing by implementing a simple parallel merge sort algorithm, and then its usage to build a smart phone factory. The work stealing technique has become mainstream and is now often considered when it comes to to dynamically balance the work load among processors. The work stealing principle in this project is synthesized as follows. Pool of Threads P1,P2,...,Pn where each thread runs a Processor, each with own queue are working in order to complete tasks in their queues. When a processor Pi is out of tasks it attempts to steal some from its neighbors..

2 Part 1: Work Stealing Scheduler:

2.1 Description
In a work stealing scheduler, each processor in the computer system has a queue of work tasks to perform. while running, each task can spawn a new task or more that can feasibly be executed in parallel with its other work, it is advised but not mandatory that these new tasks will initially put on the queue of the processor executing the task. When a processor runs out of work, it looks at the queues of other processors and steals their work items.
Each processor is a thread which maintains local work queue. A processor can push and pop tasks from its local queue. Also, a processor can pop tasks from other processor’s queue by the steal action.

2.1.1 Data Structure
The choice of the data-structure used for storing task queues is a double-ended queue, where the owner dequeues from one end and the thief dequeues from another.

2.1.2 Stealing Strategy
When it runs out of work, in order for a processor to steal from another one, it should select first a ”victim” from which it will steal the task. The victims are selected in circular manner, i.e. thief i will search for a victim in the order i + 1, i + 2, ..., N, 1, ..., i − 1 where N is the number of processors in the system (The thief starts searching
from i + 1 in each steal action) . At each stealing action, the thief steals half the number of tasks available on the
victim processor. That is, if a victim processor has n tasks, the thief can attempt to steal up to ⌊n⌋ tasks. If the 2
processor failed to steal any task, it should wait and be notified when new tasks are inserted.

2.1.3 Dependency between tasks
In some applications (e.g. the smart phones factory) the tasks have interdependence and ordering constraints. Your scheduler should fulfill these constraints. The task’s execution should be suspended until the tasks it depends on are completed.
Suspending a task should not suspend the processor, as processors and tasks are independent. When a task is suspended, the processor should continue with the next task on its queue, and the suspended task should be eventually continued only when all tasks it waits for them are done. There are two approaches to handle this:
• Rescheduling the suspended task on the same processor’s queue, it means to put the suspended task on the processor’s queue when it is ready to be continued. In some point later, the processor will fetch this task from the queue and handle it (this task can be stolen by another processor as well).
• The continuation of the task is executed directly when it is ready to be continued. The continuation is executed by any processor directly without the need to schedule the task again.
Note: When it is resumed, the suspended task should execute a continuation / callback function instead of restarting from scratch (a task might be suspended multiple times) - otherwise it will never end. Note that a task is resumed upon the completion of all the tasks it depends on (a.k.a., its child task or sub tasks).
