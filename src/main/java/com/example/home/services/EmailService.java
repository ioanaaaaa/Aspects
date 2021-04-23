package com.example.home.services;

import com.example.home.helpers.email.templates.AssignTaskTemplateEmail;
import com.example.home.helpers.email.templates.RegisterEmailTemplate;
import com.example.home.models.Task;
import com.example.home.services.observable.TaskObservableModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.*;
import java.util.*;

@Service
public class EmailService {

    @Value("${com.example.home.email.send-emails}") Boolean sendEmails;
    @Value("${com.example.home.email.host}") String host;
    @Value("${com.example.home.email.port}") String port;
    @Value("${com.example.home.email.username}") String username;
    @Value("${com.example.home.email.password}") String password;
    @Value("${com.example.home.email.from-email}") String fromEmail;
    @Value("${com.example.home.email.from-name}") String fromName;


    public void sendRegisterEmail(String toEmail) {
        this.sendEmail(toEmail,
                "You have joined HomeManagement!",
                RegisterEmailTemplate.getInstance().getTemplate(),
                null,
                null);
    }

    public void sendAssignmentTaskEmail(String userEmail, Task task){
        this.sendEmail(userEmail,
                "Task (id: " + task.getId() + ", title: " + task.getTitle() + ")",
                AssignTaskTemplateEmail.getInstance().getTemplate(),
                null,
                null);
    }

    public void sendNotificationEmailToManager(String managerEmail, TaskObservableModel taskUpdates){
        this.sendEmail(managerEmail,
                "Task (id: " + taskUpdates.getId() + ", title: " + taskUpdates.getTitle() + ")",
                "Task with name " + taskUpdates.getTitle() + " was claimed by " + taskUpdates.getUserOnTask().getFullname() + "(" + taskUpdates.getUserOnTask().getEmail() + " )"+" !",
                null,
                null);
    }

    private void sendEmail(String toEmail, String subject, String htmlContent, InputStream attachment, String attachmentName) {
        Session session = getSession();
        MimeMessage message = new MimeMessage(session);

        try {
            message.addHeader("Content-Type", "text/html; charset=UTF-8");
            message.addHeader("format", "flowed");
            message.addHeader("Content-Transfer-Encoding", "8bit");

            message.setFrom(new InternetAddress(fromEmail, fromName));
            message.setReplyTo(InternetAddress.parse(fromEmail, false));
            message.setSubject(subject, "UTF-8");
            message.setSentDate(new Date());
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));

            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(htmlContent, "UTF-8", "html");
            messageBodyPart.setDescription("task details");

            if(null != attachment) {
                DataSource source = new FileDataSource(attachmentName);
                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setFileName(attachmentName);

            }
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            message.setContent(multipart);
            Transport.send(message);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Session getSession() {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", true);
        properties.put("mail.smtp.starttls.enable", true);

        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };
        return Session.getInstance(properties, authenticator);
    }

//    public void generateDocumentTable(String pdfName, List<TransactionDto> transactions) throws IOException, DocumentException, ParseException {
//        Document document = new Document();
//
//        PrintWriter pw = new PrintWriter(pdfName + ".pdf");
//        pw.close();
//
//        PdfWriter.getInstance(document, new FileOutputStream(pdfName + ".pdf"));
//
//        document.open();
//
//        PdfPTable table = new PdfPTable(4);
//        addTableHeader(table);
//        addRows(table, transactions);
//
//        document.add(table);
//        document.close();
//    }
//
//    private void addTableHeader(PdfPTable table) {
//        Stream.of("De la user-ul", "Catre user-ul", "Suma", "Data")
//                .forEach(columnTitle -> {
//                    PdfPCell header = new PdfPCell();
//                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
//                    header.setBorderWidth(2);
//                    header.setPhrase(new Phrase(columnTitle));
//                    table.addCell(header);
//                });
//    }
//
//    private void addRows(PdfPTable table , List<TransactionDto> transactionsToDisplay) throws ParseException {
//
//        transactionsToDisplay.forEach(transaction ->  {
//            table.addCell(transaction.getFromUser().getFullname());
//            table.addCell(transaction.getToUser().getFullname());
//            table.addCell(transaction.getAmount().toString());
//            table.addCell(transaction.getDate().toString());
//        }
//        );
//    }
//
//    public void sendReportEmail(String userEmail, List<TransactionDto> transactionDtos) throws DocumentException, IOException, URISyntaxException, MessagingException, ParseException {
//        generateDocumentTable("report", transactionDtos);
//        sendEmail(userEmail, "Raport tranzactii!",new String(),new FileInputStream("report.pdf"), "report.pdf");
//    }
}
