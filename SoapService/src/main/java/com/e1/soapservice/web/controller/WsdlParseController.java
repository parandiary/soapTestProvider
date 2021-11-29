package com.e1.soapservice.web.controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.ow2.easywsdl.wsdl.WSDLFactory;
import org.ow2.easywsdl.wsdl.api.Description;
import org.ow2.easywsdl.wsdl.api.Endpoint;
import org.ow2.easywsdl.wsdl.api.Service;
import org.ow2.easywsdl.wsdl.api.WSDLException;
import org.ow2.easywsdl.wsdl.api.WSDLReader;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.e1.soapservice.web.domain.Greeting;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.predic8.schema.Schema;
import com.predic8.wsdl.AbstractSOAPBinding;
import com.predic8.wsdl.Binding;
import com.predic8.wsdl.BindingOperation;
import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.Fault;
import com.predic8.wsdl.Message;
import com.predic8.wsdl.Operation;
import com.predic8.wsdl.Part;
import com.predic8.wsdl.Port;
import com.predic8.wsdl.PortType;
import com.predic8.wsdl.WSDLParser;

import ch.qos.logback.classic.Logger;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class WsdlParseController {
	private static final String template = "Hello, %s!";
	private final AtomicLong counter = new AtomicLong();

	private final Logger log = (Logger) LoggerFactory.getLogger(this.getClass());

	@GetMapping("/greeting")
	public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
		return new Greeting(counter.incrementAndGet(), String.format(template, name));
	}


	@GetMapping("/wsdl/easyparser")
	public HashMap<String,Object> easyParser(@RequestParam(value = "url", defaultValue = "") String url) {

		HashMap<String,Object> resultMap = new HashMap<String,Object>();


		try {
	        // Read a WSDL 1.1 or 2.0
	        WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
	        Description desc = reader.read(new URL(url));

	        log.debug("desc {}" ,desc);

	        List<Service> services = desc.getServices();
	        Service service = desc.getServices().get(0);

	        log.debug("services {}" ,services);
	        log.debug("service {}" ,service);

	        // List endpoints
	        List<Endpoint> endpoints = service.getEndpoints();
	        log.debug("endpoints {}" ,endpoints);


	        // Gets address of first endpoint
	        log.debug("endpoints.get(0).getAddress() {}" ,endpoints.get(0).getAddress());

	        // Gets http method
	        log.debug("getHttpMethod {}" ,endpoints.get(0).getBinding().getBindingOperations().get(0).getHttpMethod());

	        // Gets input type
	        log.debug("getLocalPart {}" ,endpoints.get(0).getBinding().getInterface().getOperations().get(0).getInput().getElement().getType().getQName().getLocalPart());

	        //dev1 branch test


	    } catch (WSDLException | IOException | URISyntaxException e1) {
	        e1.printStackTrace();
	    }

		resultMap.put("result", "success");

		return resultMap;


	}


	@GetMapping("/wsdl/parser")
	public HashMap<String,Object> parser(@RequestParam(value = "url", defaultValue = "") String url) {
	//public HashMap<String,Object> parser(@RequestParam(value = "url") HashMap param) {

		//log.debug("Rest get /wsdl/parser param {}",param);
		//String url="";
		//if(!"".equals(param.get("url")) ) url = (String) param.get("url");
		String tUrl = url.replace("@||@", "?");
		log.debug("Rest get /wsdl/parser param {} ==> {}",url, tUrl);

		HashMap<String,Object> returnMap = new HashMap<String,Object>();
		HashMap<String,Object> resultMap = new HashMap<String,Object>();

		try {
			WSDLParser parser = new WSDLParser();

	        //Definitions defs = parser.parse("http://www.thomas-bayer.com/axis2/services/BLZService?wsdl");
	        Definitions defs = parser.parse(tUrl);

	        log.debug("-------------- WSDL Details --------------");
	        log.debug("TargenNamespace: \t" + defs.getTargetNamespace());
	        resultMap.put("TargetNamespace", defs.getTargetNamespace());


	        if (defs.getDocumentation() != null) {
	        	log.debug("Documentation: \t\t" + defs.getDocumentation());
	        }
	        log.debug("\n");

	        /* For detailed schema information see the FullSchemaParser.java sample.*/
	        log.debug("Schemas: ");
	        List<HashMap> schemas = new ArrayList<HashMap>();
	        for (Schema schema : defs.getSchemas()) {
	        	log.debug("  TargetNamespace: \t" + schema.getTargetNamespace());
	        	HashMap<String,Object> smap = new HashMap<String,Object>();
	        	smap.put("TargetNamespace", schema.getTargetNamespace());
	        	schemas.add(smap);
	        }
	        resultMap.put("schemes", schemas);


	        log.debug("\n");

	        log.debug("Messages: ");
	        List<HashMap> messageList = new ArrayList<HashMap>();
	        for (Message msg : defs.getMessages()) {
	        	log.debug("  Message Name: " + msg.getName());
	        	HashMap<String,Object> messageMap = new HashMap<String,Object>();
	        	messageMap.put("name", msg.getName());


	        	log.debug("  Message Parts: ");
	        	List<HashMap> msgPartList = new ArrayList<HashMap>();
	            for (Part part : msg.getParts()) {

	            	log.debug("    Part Name: " + part.getName());
	            	log.debug("    Part Element: " + ((part.getElement() != null) ? part.getElement() : "not available!"));
	            	log.debug("    Part Type: " + ((part.getType() != null) ? part.getType() : "not available!" ));
	            	log.debug("");
	            	HashMap<String,Object> partsMap = new HashMap<String,Object>();
	            	partsMap.put("name", part.getName());
	            	partsMap.put("elementStr", ((part.getElement() != null) ? part.getElement().toString() : "not available!"));
	            	partsMap.put("typeStr", ((part.getType() != null) ? part.getType().toString() : "not available!" ));

	            	msgPartList.add(partsMap);
	            }
	            messageMap.put("parts", msgPartList);
	            messageList.add(messageMap);
	        }
	        resultMap.put("messages", messageList);
	        //log.debug(">>messageList {}",messageList);
	        log.debug("");


//	        log.debug("=========  check ================");
//	        for (int i = 0; i < messageList.size(); i++) {
//	        	log.debug("messageList.get({}) {}",i, messageList.get(i));
//
//			}





	        log.debug("PortTypes: ");
	        List<HashMap> portTypesList = new ArrayList<HashMap>();
	        for (PortType pt : defs.getPortTypes()) {
	        	log.debug("  PortType Name: " + pt.getName());
	        	log.debug("  PortType Operations: ");
	        	HashMap<String,Object> portTypeMap = new HashMap<String,Object>();
	        	portTypeMap.put("name", pt.getName());

	        	List<HashMap> operationList = new ArrayList<HashMap>();
	            for (Operation op : pt.getOperations()) {
	            	HashMap<String,Object> portTypeOperationMap = new HashMap<String,Object>();

	            	log.debug("    Operation Name: " + op.getName());
	            	log.debug("    Operation Input Name: "
	                    + ((op.getInput().getName() != null) ? op.getInput().getName() : "not available!"));
	            	log.debug("    Operation Input Message: "
	                    + op.getInput().getMessage().getQname());
	            	log.debug("    Operation Output Name: "
	                    + ((op.getOutput().getName() != null) ? op.getOutput().getName() : "not available!"));
	            	log.debug("    Operation Output Message: "
	                    + op.getOutput().getMessage().getQname());
	            	log.debug("    Operation Faults: ");
	                if (op.getFaults().size() > 0) {
	                    for (Fault fault : op.getFaults()) {
	                    	log.debug("      Fault Name: " + fault.getName());
	                    	log.debug("      Fault Message: " + fault.getMessage().getQname());
	                    }
	                } else log.debug("      There are no faults available!");


	                portTypeOperationMap.put("name", op.getName());
	                portTypeOperationMap.put("inputName", ((op.getInput().getName() != null) ? op.getInput().getName() : "not available!"));
	                portTypeOperationMap.put("outputName", ((op.getOutput().getName() != null) ? op.getOutput().getName() : "not available!"));
	                portTypeOperationMap.put("inputMsg", op.getInput().getMessage().getQname());
	                portTypeOperationMap.put("outputMsg", op.getOutput().getMessage().getQname());
	                portTypeOperationMap.put("name", op.getName());
	                operationList.add(portTypeOperationMap);



	            }
	            portTypeMap.put("operations", operationList);

	            portTypesList.add(portTypeMap);

	            log.debug("");
	        }
	        resultMap.put("portTypes", portTypesList);
	        log.debug("");




	        log.debug("Bindings: ");
	        List<HashMap> bindingList = new ArrayList<HashMap>();

	        for (Binding bnd : defs.getBindings()) {
	        	HashMap<String,Object> bindingMap = new HashMap<String,Object>();
	        	log.debug("  Binding Name: " + bnd.getName());
	        	log.debug("  Binding Type: " + bnd.getPortType().getName());
	        	log.debug("  Binding Protocol: " + bnd.getBinding().getProtocol());
	            if(bnd.getBinding() instanceof AbstractSOAPBinding) log.debug("  Style: " + (((AbstractSOAPBinding)bnd.getBinding()).getStyle()));

	            bindingMap.put("name", bnd.getName());
	            bindingMap.put("type", bnd.getPortType().getName());
	            bindingMap.put("protocol", bnd.getBinding().getProtocol());
	            bindingMap.put("style", "");
	            if(bnd.getBinding() instanceof AbstractSOAPBinding) bindingMap.put("style", (((AbstractSOAPBinding)bnd.getBinding()).getStyle()));


	            log.debug("  Binding Operations: ");
	            List<HashMap> bindingOpList = new ArrayList<HashMap>();
	            for (BindingOperation bop : bnd.getOperations()) {
	            	HashMap<String,Object> bindingOpMap = new HashMap<String,Object>();
	            	bindingOpMap.put("name", bop.getName());
	            	bindingOpMap.put("SoapAction", "");
	            	bindingOpMap.put("SoapBodyUse", "");

	            	log.debug("    Operation Name: " + bop.getName());
	                if(bnd.getBinding() instanceof AbstractSOAPBinding) {
	                	log.debug("    Operation SoapAction: " + bop.getOperation().getSoapAction());
	                	log.debug("    SOAP Body Use: " + bop.getInput().getBindingElements().get(0).getUse());
	                	bindingOpMap.put("SoapAction", bop.getOperation().getSoapAction());
		            	bindingOpMap.put("SoapBodyUse", bop.getInput().getBindingElements().get(0).getUse());
	                }

	                bindingOpList.add(bindingOpMap);
	            }
	            bindingMap.put("operations", bindingOpList);
	            bindingList.add(bindingMap);
	            log.debug("");
	        }
	        resultMap.put("bindings", bindingList);
	        log.debug("");



	        log.debug("Services: ");
	        List<HashMap> serviceList = new ArrayList<HashMap>();
	        for (com.predic8.wsdl.Service service : defs.getServices()) {
	        	HashMap<String,Object> serviceMap = new HashMap<String,Object>();
	        	log.debug("  Service Name: " + service.getName());
	        	serviceMap.put("name", service.getName());

	        	log.debug("  Service Potrs: ");
	        	List<HashMap> portList = new ArrayList<HashMap>();
	            for (Port port : service.getPorts()) {
	            	HashMap<String,Object> portMap = new HashMap<String,Object>();
	            	log.debug("    Port Name: " + port.getName());
	            	log.debug("    Port Binding: " + port.getBinding().getName());
	            	log.debug("    Port Address Location: " + port.getAddress().getLocation()+ "\n");

	            	portMap.put("name", port.getName());
	            	portMap.put("Binding", port.getBinding().getName());
	            	portMap.put("Location", port.getAddress().getLocation());

	            	portList.add(portMap);
	            }
	            serviceMap.put("ports", portList);
	            serviceList.add(serviceMap);
	        }
	        resultMap.put("services", serviceList);
	        log.debug("");


		} catch (Exception e1) {
			e1.printStackTrace();
		}


		returnMap.put("result", resultMap);

		log.debug("returnMap {}",returnMap);

		//test json
		Gson gson = new Gson();
        JsonObject json = gson.toJsonTree(returnMap).getAsJsonObject();
        log.debug("json {}",json);

		//return json;
		return returnMap;


	}
}






