package eu.openminted.registry.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.openminted.registry.services.DumpService;

@RestController
public class DumpController {
	
	@Autowired
	DumpService dumpService;
	
	@RequestMapping(value = "/dump/", method = RequestMethod.GET)  
	@ResponseBody public void dumpAll(HttpServletRequest request, HttpServletResponse response) {  

		 	ServletContext context = request.getServletContext();
	        String appPath = context.getRealPath("");
	        System.out.println("appPath = " + appPath);
	 
	        // construct the complete absolute path of the file
	        File downloadFile = dumpService.bringAll();
	        FileInputStream inputStream;
			try {
				inputStream = new FileInputStream(downloadFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return;
			}
	         
	        // get MIME type of the file
	        String mimeType = context.getMimeType(downloadFile.getAbsolutePath());
	        if (mimeType == null) {
	            // set to binary type if MIME mapping not found
	            mimeType = "application/octet-stream";
	        }
	        System.out.println("MIME type: " + mimeType);
	 
	        // set content attributes for the response
	        response.setContentType(mimeType);
	        response.setContentLength((int) downloadFile.length());
	 
	        // set headers for the response
	        String headerKey = "Content-Disposition";
	        String headerValue = String.format("attachment; filename=\"%s\"",
	                downloadFile.getName());
	        response.setHeader(headerKey, headerValue);
	 
	        // get output stream of the response
	        OutputStream outStream;
			try {
				outStream = response.getOutputStream();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
	 
	        byte[] buffer = new byte[4096];
	        int bytesRead = -1;
	 
	        // write bytes read from the input stream into the output stream
	        try {
				while ((bytesRead = inputStream.read(buffer)) != -1) {
				    outStream.write(buffer, 0, bytesRead);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	 
	        try {
				inputStream.close();
				outStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	        DumpService.deleteDirectory(new File("/home/user/tmp/dump-testCase"));
		
    } 
	
	@RequestMapping(value = "/dump/{resourceType}", method = RequestMethod.GET)  
	@ResponseBody public void dumpResourceType(@PathVariable("resourceType") String resourceType, HttpServletRequest request, HttpServletResponse response) {  

		 	ServletContext context = request.getServletContext();
	        String appPath = context.getRealPath("");
	        System.out.println("appPath = " + appPath);
	 
	        // construct the complete absolute path of the file
	        File downloadFile = dumpService.bringResourceType(resourceType);
	        FileInputStream inputStream;
			try {
				inputStream = new FileInputStream(downloadFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return;
			}
	         
	        // get MIME type of the file
	        String mimeType = context.getMimeType(downloadFile.getAbsolutePath());
	        if (mimeType == null) {
	            // set to binary type if MIME mapping not found
	            mimeType = "application/octet-stream";
	        }
	        System.out.println("MIME type: " + mimeType);
	 
	        // set content attributes for the response
	        response.setContentType(mimeType);
	        response.setContentLength((int) downloadFile.length());
	 
	        // set headers for the response
	        String headerKey = "Content-Disposition";
	        String headerValue = String.format("attachment; filename=\"%s\"",
	                downloadFile.getName());
	        response.setHeader(headerKey, headerValue);
	 
	        // get output stream of the response
	        OutputStream outStream;
			try {
				outStream = response.getOutputStream();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
	 
	        byte[] buffer = new byte[4096];
	        int bytesRead = -1;
	 
	        // write bytes read from the input stream into the output stream
	        try {
				while ((bytesRead = inputStream.read(buffer)) != -1) {
				    outStream.write(buffer, 0, bytesRead);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	 
	        try {
				inputStream.close();
				outStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	        DumpService.deleteDirectory(new File("/home/user/tmp/dump-testCase"));
		
    } 

}