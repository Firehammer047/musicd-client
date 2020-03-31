/*
	musicd-client is an Android app for communicating
	with the musicd server running on a computer.

	Copyright (c) 2015 GB Tony Cabrera
*/
package com.RDFM.musicd;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.util.Log;
import android.os.AsyncTask;

import java.io.*;
import java.net.*;
import java.util.*;

public class MainActivity extends Activity
{
//    private Handler myhandler = new Handler();
	
	private static final String APP_NAME = "musicd";
	static boolean RUNNING = false;
	static int SLEEP_TIME = 2000;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		TextView text_info = (TextView)findViewById(R.id.text_info);
		String ip = getLocalIpAddress();
		text_info.setText(ip);
		Log.d(APP_NAME, "Running = " + RUNNING);
		if(!RUNNING){
			Log.d(APP_NAME, "Starting thread...");
			Thread UIthread = new Thread(new updateUIThread("filename"));
			UIthread.start();
		}
    }
	
	private String getLocalIpAddress() {
		try {
			NetworkInterface net = NetworkInterface.getByName("wlan0");
			String if_name = net.getName();
			Log.d(APP_NAME, if_name);
			Enumeration<InetAddress> inetAddresses = net.getInetAddresses();
			String ip_address="";
			for (InetAddress inetAddress : Collections.list(inetAddresses)) {
				ip_address = inetAddress.getHostAddress();
				Log.d(APP_NAME, ip_address);
			}
			return if_name + " : " + ip_address;
		} catch (SocketException e) {
			Log.e(APP_NAME, e.toString());
		}
		return null;
	}


	public void next(View view){
		Thread cThread = new Thread(new ClientThread("n"));
		cThread.start();
	}

	public void pause(View view){
		Thread cThread = new Thread(new ClientThread("p"));
		cThread.start();
	}
	
	public void back(View view){
		Thread cThread = new Thread(new ClientThread("b"));
		cThread.start();
	}
	
	public void volumeDown(View view){
		Thread cThread = new Thread(new ClientThread("vol70"));
		cThread.start();
	}
	
	public void volumeUp(View view){
		Thread cThread = new Thread(new ClientThread("vol100"));
		cThread.start();
	}


	
	
	public class ClientThread implements Runnable {
		private String cmd;
		private byte[] cmd_bytes;
		private String reply;

		public ClientThread(String s){
			this.cmd = s;
		}

		public void run() {
		
			String host = "192.168.2.112";
			int port = 6969;
			Log.d(APP_NAME, "Trying " + host);
			try{
				Socket s = new Socket(host, port);
				Log.d(APP_NAME, "Connected.");
				
				PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
				out.write(cmd);
				out.write("\r\n");
				out.flush();
				
				BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
				reply = in.readLine();
				Log.d(APP_NAME, "Server reply: " + reply);
				
				s.close();
			} catch (UnknownHostException e) {
				String st = Log.getStackTraceString(e);
			} catch (IOException e) {
				String st = Log.getStackTraceString(e);
			}
		}
	}

	public class updateUIThread implements Runnable {
		private String cmd;
		private String reply;

		public updateUIThread(String s){
			this.cmd = s;
			MainActivity.RUNNING = true;
		}

		public void run() {
		
			String host = "192.168.2.112";
			int port = 6969;
			while(true){
				Log.d(APP_NAME, "Trying " + host);
				try{
					Socket s = new Socket(host, port);
					Log.d(APP_NAME, "Connected.");
				
					PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
					out.write(cmd);
					out.write("\r\n");
					out.flush();
				
					BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
					reply = in.readLine();
					Log.d(APP_NAME, "Server reply: " + reply);
				
					MainActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							TextView song_info = (TextView)findViewById(R.id.song_info);
							song_info.setText(reply);
						}
					});

					s.close();
					try{
						Thread.sleep(SLEEP_TIME);
					} catch (Exception e) {}
				}
				catch (UnknownHostException e) {
					String st = Log.getStackTraceString(e);
					Log.d(APP_NAME, "ERROR: " + st);
				} catch (IOException e) {
					String st = Log.getStackTraceString(e);
				}
			}
		}
	}







// # END
}
