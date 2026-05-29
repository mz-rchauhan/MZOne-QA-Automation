package utils;

import org.jsoup.Jsoup;
import org.testng.Assert;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.SubjectTerm;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

public class GmailUtils {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GmailUtils.class);
    private static final String IMAP_PROTOCOL = "imaps";
    private static final String INBOX_FOLDER = "inbox";
    private static final String IMAP_HOST = "imap.gmail.com";
    private static final String STORE_TYPE = "imaps";
    private final List<InternetAddress> ccList = new ArrayList<>();

    public Store getMailStore() throws Exception {
        Properties props = new Properties();
        String configPath = System.getProperty("user.dir") + File.separator + "smtp.properties";

        try (FileInputStream fis = new FileInputStream(configPath)) {
            props.load(fis);
        }

        Session session = Session.getInstance(props, null);
        Store store = session.getStore(IMAP_PROTOCOL);
        store.connect(
                props.getProperty("mail.smtp.host"),
                props.getProperty("mail.username"),
                props.getProperty("mail.password"));
        return store;
    }

    public Map<String, String> fetchEmail(String mailSubject, String identifier) {
        Map<String, String> emailData = new HashMap<>();
        boolean found = false;

        try (Store store = getMailStore();
                Folder inbox = store.getFolder(INBOX_FOLDER)) {

            inbox.open(Folder.READ_ONLY);
            Message[] messages = inbox.getMessages();

            for (int i = messages.length - 1; i >= 0; i--) {
                Message msg = messages[i];
                String body = getTextFromMessage(msg);

                if (isTargetEmail(msg, body, mailSubject, identifier)) {
                    emailData.put("Subject", msg.getSubject());
                    emailData.put("Sender", ((InternetAddress) msg.getFrom()[0]).getAddress());
                    emailData.put("Body", cleanBodyContent(body, mailSubject));

                    captureCCRecipients(msg);
                    found = true;
                    break;
                }
            }

            if (!found) {
                Assert.fail("Email with subject [" + mailSubject + "] and identifier [" + identifier + "] not found.");
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch email: " + e.getMessage(), e);
        }
        return emailData;
    }

    public void verifyAbsenceOfEmail(String mailSubject, String identifier) {
        try (Store store = getMailStore();
                Folder inbox = store.getFolder(INBOX_FOLDER)) {

            inbox.open(Folder.READ_ONLY);
            Message[] messages = inbox.getMessages();

            for (Message msg : messages) {
                if (msg.getSubject().contains(mailSubject) && getTextFromMessage(msg).contains(identifier)) {
                    Assert.fail("Email was found but was expected to be absent: " + mailSubject);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error during email absence verification", e);
        }
    }

    public void purgeInbox() {
        try (Store store = getMailStore();
                Folder inbox = store.getFolder(INBOX_FOLDER)) {

            inbox.open(Folder.READ_WRITE);
            Message[] messages = inbox.getMessages();

            for (Message msg : messages) {
                if (!msg.getSubject().contains("Expiration Reminder")) {
                    msg.setFlag(Flags.Flag.DELETED, true);
                }
            }
            inbox.close(true);
        } catch (Exception e) {
            LOGGER.error("Failed to purge inbox", e);
        }
    }

    private boolean isTargetEmail(Message msg, String body, String subject, String id) throws MessagingException {
        String msgSubject = msg.getSubject();
        return (msgSubject.contains(subject) && (body.contains(id) || msgSubject.contains(id)));
    }

    private void captureCCRecipients(Message msg) throws MessagingException {
        Address[] recipients = msg.getRecipients(Message.RecipientType.CC);
        if (recipients != null) {
            for (Address addr : recipients) {
                ccList.add((InternetAddress) addr);
            }
        }
    }

    private String getTextFromMessage(Message message) throws Exception {
        if (message.isMimeType("text/plain") || message.isMimeType("text/html")) {
            return message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            return getTextFromMimeMultipart((MimeMultipart) message.getContent());
        }
        return "";
    }

    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws Exception {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < mimeMultipart.getCount(); i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result.append(bodyPart.getContent());
            } else if (bodyPart.isMimeType("text/html")) {
                result.append(Jsoup.parse((String) bodyPart.getContent()).text());
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result.append(getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent()));
            }
        }
        return result.toString();
    }

    private String cleanBodyContent(String body, String subject) {
        String cleaned = body.replaceAll("\\r|\\n|</li>|</ol>|<ol>|<li>", " ");
        if (cleaned.contains("site.com")) {
            cleaned = cleaned.split("Thank you,")[0];
        }
        return cleaned.trim().replaceAll(" +", " ");
    }

    /* NEW: Extraction Utilities from Framework B */

    public String extractTextBetween(String body, String start, String end) {
        int startIndex = body.indexOf(start);
        if (startIndex == -1)
            return null;
        startIndex += start.length();
        int endIndex = body.indexOf(end, startIndex);
        if (endIndex == -1)
            return null;
        return body.substring(startIndex, endIndex).trim();
    }

    public String extractOTP(String body, int length) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\b\\d{" + length + "}\\b");
        java.util.regex.Matcher matcher = pattern.matcher(body);
        return matcher.find() ? matcher.group() : null;
    }

    public String extractLink(String body) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("https?://\\S+");
        java.util.regex.Matcher matcher = pattern.matcher(body);
        return matcher.find() ? matcher.group() : null;
    }

    public static String fetchAndReadEmailBySubject(String username, String appPassword, String subjectKeyword) {
        Properties properties = new Properties();
        properties.put("mail.imap.host", IMAP_HOST);
        properties.put("mail.imap.port", "993");
        properties.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.imap.socketFactory.fallback", "false");
        properties.setProperty("mail.imap.socketFactory.port", "993");

        Session session = Session.getDefaultInstance(properties);
        try {
            Store store = session.getStore(STORE_TYPE);
            store.connect(IMAP_HOST, username, appPassword);

            Folder inbox = store.getFolder("inbox");
            inbox.open(Folder.READ_ONLY);

            Message[] messages = inbox.search(new SubjectTerm(subjectKeyword));
            if (messages.length > 0) {
                Message msg = messages[messages.length - 1]; // Latest
                Object content = msg.getContent();
                String body = content != null ? content.toString() : "";
                LOGGER.info("Successfully fetched email with subject: " + subjectKeyword);
                inbox.close(false);
                store.close();
                return body;
            }

            inbox.close(false);
            store.close();
        } catch (Exception e) {
            LOGGER.error("IMAP Fetch failed", e);
        }
        return "";
    }
}
