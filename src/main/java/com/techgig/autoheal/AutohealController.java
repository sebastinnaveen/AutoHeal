package com.techgig.autoheal;

import org.springframework.web.bind.annotation.RestController;



import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.techgig.autoheal.services.ConfigBean;
import com.techgig.autoheal.services.ContextConfiguration;
import com.techgig.autoheal.services.SampleProperty;
import com.techgig.autoheal.utils.JsonPropertySourceLoader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
@RestController
public class AutohealController {
	 @Autowired
	    private ContextConfiguration configBean;
	 HttpURLConnection connection = null;
	 @Autowired
	    private SampleProperty sampleProperty;
	    @Value("${prefix.stringProp1}")
	    private String stringProp1;
	
	    
	    @RequestMapping("/Slack")
	    public String slackapi() {
	    	   try {
	    	         // Create connection
	    	         final URL url = new URL("https://hooks.slack.com/services/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");
	    	         connection = (HttpURLConnection) url.openConnection();
	    	         connection.setRequestMethod("POST");
	    	         connection.setConnectTimeout(5000);
	    	         connection.setUseCaches(false);
	    	         connection.setDoInput(true);
	    	         connection.setDoOutput(true);

	    	         final String payload = "payload="
	    	                 + URLEncoder.encode("{\"channel\":\"#devops\",\"username\":\"webhookbot\",\"text\":\"Test message from Naveen #devops and comes from a bot named webhookbot\",\"icon_emoji\":\":ghost:\"}", "UTF-8");

	    	         // Send request
	    	         final DataOutputStream wr = new DataOutputStream(
	    	                 connection.getOutputStream());
	    	         wr.writeBytes(payload);
	    	         wr.flush();
	    	         wr.close();
	    	         System.out.println(sampleProperty.getStringProp1());
	    	         // Get Response
	    	         final InputStream is = connection.getInputStream();
	    	         final BufferedReader rd = new BufferedReader(new InputStreamReader(is));
	    	         String line;
	    	         StringBuilder response = new StringBuilder();
	    	         while ((line = rd.readLine()) != null) {
	    	             response.append(line);
	    	             response.append('\n');
	    	         }

	    	         rd.close();
	    	         return response.toString();
	    	     } catch (Exception e) {
	    	        e.printStackTrace();
	    	        return "error--->"+e.getMessage();
	    	     } finally {
	    	         if (connection != null) {
	    	             connection.disconnect();
	    	         }
	    	     }
	       // return "Successfully Posted in Slack!";
	   
	    
	}
	    
	    
	    
	    @RequestMapping("/Zabbix")
	    public String zabbixtrigger() {
	    	String host="ssh.journaldev.com";
		    String user="sshuser";
		    String password="sshpwd";
		    String command1="ls -ltr";
		    try{
		    	
		    	java.util.Properties config = new java.util.Properties(); 
		    	config.put("StrictHostKeyChecking", "no");
		    	JSch jsch = new JSch();
		    	Session session=jsch.getSession(user, host, 22);
		    	session.setPassword(password);
		    	session.setConfig(config);
		    	session.connect();
		    	System.out.println("Connected");
		    	
		    	Channel channel=session.openChannel("exec");
		        ((ChannelExec)channel).setCommand(command1);
		        channel.setInputStream(null);
		        ((ChannelExec)channel).setErrStream(System.err);
		        
		        InputStream in=channel.getInputStream();
		        channel.connect();
		        byte[] tmp=new byte[1024];
		        while(true){
		          while(in.available()>0){
		            int i=in.read(tmp, 0, 1024);
		            if(i<0)break;
		            System.out.print(new String(tmp, 0, i));
		          }
		          if(channel.isClosed()){
		            System.out.println("exit-status: "+channel.getExitStatus());
		            break;
		          }
		          try{Thread.sleep(1000);}catch(Exception ee){}
		        }
		        channel.disconnect();
		        session.disconnect();
		        System.out.println("DONE");
		    }catch(Exception e){
		    	e.printStackTrace();
		    }
	   
	    return "Zabbix Triggered Successfully";
	}
}
