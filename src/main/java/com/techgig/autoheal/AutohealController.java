package com.techgig.autoheal;

import org.springframework.web.bind.annotation.RestController;



import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.techgig.autoheal.services.ConfigBean;

import com.techgig.autoheal.services.SampleProperty;
import com.techgig.autoheal.utils.JsonPropertySourceLoader;
import com.verizon.model.ZabbixInputs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
@RestController
public class AutohealController {
	
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
	    	                 + URLEncoder.encode("{\"channel\":\"#devops\",\"username\":\"Sizzler-DevOps\",\"text\":\"Test message from Naveen #devops\",\"icon_emoji\":\":happy:\"}", "UTF-8");

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
	    
	    
	    
	  //  @RequestMapping("/Zabbix/{action:.+}")
	  //  @RequestMapping("/Zabbix/{action:,+}")
	    //,
	    
	    @RequestMapping(
	    	    value = "/Zabbix", 
	    	    method = RequestMethod.POST,
	    	    consumes = "application/json")
	    
	   // public String zabbixtrigger(@PathVariable(value="action") String act) {
	    public String zabbixtrigger(@RequestBody String payload	) {
	    //@RequestBody String payload	
	    	//System.out.println(request.getparameter("action"));
	    	String host="169.254.7.54";
		    String user="ramkumar";
		    String password="root";
		    String command1="ansible-playbook site.yml";
		    try{
		    	System.out.println("action---->"+payload);
		    	ObjectMapper mapper = new ObjectMapper();
		    	ZabbixInputs zabInpu = mapper.readValue(payload, ZabbixInputs.class);
		    	if(payload!=null&& !("".equalsIgnoreCase(payload))){
		    		 triggerSlack(zabInpu.getServer(),zabInpu.getName()," Self healed started ");
		    	}
		    	
		    	java.util.Properties config = new java.util.Properties(); 
		    	config.put("StrictHostKeyChecking", "no");
		    	JSch jsch = new JSch();
		    	Session session=jsch.getSession(user, host, 22);
		    	session.setPassword(password);
		    	session.setConfig(config);
		    	session.connect();
//		    	System.out.println("Connected");
////		    	
		    	Channel channel=session.openChannel("exec");
		        ((ChannelExec)channel).setCommand(command1);
		        channel.setInputStream(null);
		        ((ChannelExec)channel).setErrStream(System.err);
////		        
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
//		    	
		    	if(payload!=null&& !("".equalsIgnoreCase(payload)) && channel.getExitStatus()==0){
		    		 triggerSlack(zabInpu.getServer(),zabInpu.getName()," Self healed successfully ");
		    	}
		    	else{
		    		 triggerSlack(zabInpu.getServer(),zabInpu.getName()," Unable to selfheal, please look into that immediately.");
		    	}
		    }catch(Exception e){
		    	e.printStackTrace();
		    }
		    return "Action--->" +payload+ " Triggered Successfully";
	    //return "Zabbix Triggered Successfully";
	}
	    
	    
	    public void triggerSlack(String server,String action, String healStatus){
	    	try{
	    	 final URL url = new URL("https://hooks.slack.com/services/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");
	         connection = (HttpURLConnection) url.openConnection();
	         connection.setRequestMethod("POST");
	         connection.setConnectTimeout(5000);
	         connection.setUseCaches(false);
	         connection.setDoInput(true);
	         connection.setDoOutput(true);

	         final String payload = "payload="
	                 + URLEncoder.encode("{\"channel\":\"#devops\",\"username\":\"Sizzler-DevOps\",\"text\":\"Action: In server "+server+" " +action+ healStatus+" ...\",\"icon_emoji\":\":happy:\"}", "UTF-8");

	         // Send request
	         final DataOutputStream wr = new DataOutputStream(
	                 connection.getOutputStream());
	         wr.writeBytes(payload);
	         wr.flush();
	         wr.close();
	     //    System.out.println(sampleProperty.getStringProp1());
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
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	    }
}
