package bgu.spl.a2.test;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import bgu.spl.a2.Task;
import bgu.spl.a2.WorkStealingThreadPool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class MergeSort extends Task<int[]> {

	private final int[] array;

	public MergeSort(int[] array) {
		this.array = array;
	}

	@Override
	protected void start() {
		if(array.length==1)
			complete(array);
		else{
			ArrayList<Task<int[]>> tasks = new ArrayList<>();
			int[] arr1=new int[array.length/2];
			int[] arr2=new int[(array.length - array.length/2)];
			for(int i=0;i<arr1.length;i++){
				arr1[i]=array[i];
			}
			for(int i=0;i<arr2.length;i++){
				arr2[i]=array[i+arr1.length];
			}
			MergeSort sonA=new MergeSort(arr1);
			MergeSort sonB=new MergeSort(arr2);
			tasks.add(sonA);
			tasks.add(sonB);
			spawn(sonA,sonB);
			whenResolved(tasks,()->{
				int[] sortedArr=new int[array.length] ;
				int[] arrA = tasks.get(0).getResult().get();
				int[] arrB = tasks.get(1).getResult().get();
				int counterA = 0, counterB = 0, mainCounter = 0;
				while (counterA< arrA.length & counterB < arrB.length){
					if (arrA[counterA]<arrB[counterB]){ //current A element is smaller
						sortedArr[mainCounter] = arrA[counterA];
						counterA++;
					}
					else{
						sortedArr[mainCounter] = arrB[counterB];
						counterB++;
					}
					mainCounter++;
				}
				for (int i = counterA; i < arrA.length; i++){
					sortedArr[mainCounter] = arrA[i];
					mainCounter++;
				}
				for (int j = counterB; j < arrB.length ; j++){
					sortedArr[mainCounter] = arrB[j];
					mainCounter++;
				}
				complete(sortedArr);
			}
					);
		}
	}

	public static void main(String[] args) throws InterruptedException {
		WorkStealingThreadPool pool = new WorkStealingThreadPool(4);
		int n = 1000000; //you may check on different number of elements if you like
		int[] array = new Random().ints(n).toArray();

		MergeSort task = new MergeSort(array);

		CountDownLatch l = new CountDownLatch(1);
		pool.start();
		pool.submit(task);
		task.getResult().whenResolved(() -> {
			//warning - a large print!! - you can remove this line if you wish
			System.out.println(Arrays.toString(task.getResult().get()));
			l.countDown();
		});

		l.await();
		pool.shutdown();
	}
}