# NDKStudy
A small code sample to show how to work with NDK

It attempts to apply a Relief effect to an image choosen from the gallery, using Java and C++, and compare the time taken to complete the task.

The image process is done in a binded Service, as the process can take a long time if use Java, we need to pass the Bitmap back to activity.

The loading image from gallery process is done using AsycnTask, as it is very fast.




