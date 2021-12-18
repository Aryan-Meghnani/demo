package com.example.demo.controller;



import com.aspose.words.MsWordVersion;
import com.aspose.words.PdfCompliance;
import com.aspose.words.PdfSaveOptions;
import com.lowagie.text.DocumentException;
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;

import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@Log4j2
@RequestMapping("/demo")
public class MainController {

    @PostMapping(value = "/request", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> request() throws Exception {
        generatePdf();
        return new ResponseEntity<>("SUCCESSFUL", HttpStatus.OK);
    }

    public void generatePdf() throws IOException, DocumentException {
        String vm_path  = "/home/perennial/Documents/";//Path of the template makesure to give absolutePath eg.  C:/Velocity_Template/
        String vm_template = "mainFile.vm"; //file name with ".vm" extention.
//        String vm_template = "new_template.vm"; //file name with ".vm" extention.
        //this can be created as bean as well but
        // make sure all the templates are from same path
        VelocityEngine velocityEngine =  new VelocityEngine();
        Properties properties =  new Properties();
        properties.put("file.resource.loader.path", vm_path);
        velocityEngine.init(properties);
        List<String> address = Arrays.asList("Brooklyn");
        List<Person> person = new ArrayList<>();
        person.add(new Person("JOhne","939","Director","gmail.com"));
        person.add(new Person("Adam","9339","Sharehholder","gmail.com"));
        //Write data to template
        StringWriter stringWriter = new StringWriter();
        VelocityContext context = new VelocityContext();
        context.put("date","26-11-2021");
        context.put("companyName","Validus");
        context.put("reg","201530032R");
        context.put("list",person);
        velocityEngine.mergeTemplate(vm_template,"UTF-8",context,stringWriter);
        String html = stringWriter.toString();
        stringWriter.close();
        //Parsing the html content to XML which is used by flyingsauser to convert to PDF
        Document document = Jsoup.parse(html);
        document.outputSettings()
                .syntax(Document.OutputSettings.Syntax.xml).prettyPrint(true);

        String PDF = "/home/perennial/Documents/sample.pdf" ;// pdf location
        File outputPdf = new File(PDF);
        OutputStream outputStream = new FileOutputStream(outputPdf);
//Rendring Html to PDF
        ITextRenderer renderer = new ITextRenderer();
        SharedContext sharedContext = renderer.getSharedContext();
        sharedContext.setPrint(true);
        sharedContext.setInteractive(true);
        renderer.setDocumentFromString(document.html());
        renderer.layout();
        renderer.createPDF(outputStream,true,1);
        outputStream.flush();
        outputStream.close();

    }








//    void convertPdf(){
//        try {
//            InputStream in = new FileInputStream(new File("/home/perennial/Documents/finalDocument.docx"));
//             OutputStream out = new FileOutputStream(new File("/home/perennial/Documents/finalDocument.pdf"));
//            long start = System.currentTimeMillis();
//            // 1) Load DOCX into XWPFDocument
//            XWPFDocument docFile = new XWPFDocument(in);
//            // 2) Prepare Pdf options
//            PdfOptions options = PdfOptions.create();
//            // 3) Convert XWPFDocument to Pdf
//            PdfConverter.getInstance().convert(docFile, out, options);
//            System.out.println("word-sample.docx was converted to a PDF file in :: "
//                    + (System.currentTimeMillis() - start) + " milli seconds");
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }
//    }
//
//    void convertToPdf() throws Exception {
//        Document doc = new Document("/home/perennial/Documents/finalDocument.docx");
//        PdfSaveOptions pso = new PdfSaveOptions();
//        doc.getCompatibilityOptions().optimizeFor(MsWordVersion.WORD_2003);
//// Save the document in PDF format.
//        doc.save("/home/perennial/Documents/finalDocument.pdf");
//
//    }
//
//    void date(){
//        Date date = new Date();
//        long unixTimeDateCreated = date.getTime() / 1000L;
//
//        Calendar c = Calendar.getInstance();
//        c.setTime(date);
//        c.add(Calendar.DATE, 7);
//
//        Date date1=c.getTime();
//        long unixTimeExpireDate = date1.getTime() /1000L;
//    }
//
//  String getPdfInBase64() throws IOException, NoSuchAlgorithmException {
//        byte[] inFileBytes = Files.readAllBytes(Paths.get("/home/perennial/Documents/finalDocument.pdf"));
//        byte[] encoded = org.apache.commons.codec.binary.Base64.encodeBase64(inFileBytes);
//
//        String pdfInBase64 = new String(encoded);
////        System.out.println(pdfInBase64);
//      System.out.println();
//      System.out.println("Hash of Doc: " + "\n" + bytesToHex(checksum("/home/perennial/Documents/finalDocument.pdf", "SHA3-256")));
//        return pdfInBase64;
//    }
//
//
//    private static byte[] checksum(String filePath, String algorithm) {
//
//        MessageDigest md;
//        try {
//            md = MessageDigest.getInstance(algorithm);
//        } catch (NoSuchAlgorithmException e) {
//            throw new IllegalArgumentException(e);
//        }
//
//        try (InputStream is = new FileInputStream(filePath);
//             DigestInputStream dis = new DigestInputStream(is, md)) {
//            while (dis.read() != -1) ; //empty loop to clear the data
//            md = dis.getMessageDigest();
//        } catch (IOException e) {
//            throw new IllegalArgumentException(e);
//        }
//        return md.digest();
//
//    }
//
//    public static String bytesToHex(byte[] bytes) {
//        StringBuilder sb = new StringBuilder();
//        for (byte b : bytes) {
//            sb.append(String.format("%02x", b));
//        }
//        return sb.toString();
//    }


}


