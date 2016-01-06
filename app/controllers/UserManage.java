package controllers;

import com.larvalabs.redditchat.Constants;
import com.larvalabs.redditchat.util.TopAlert;
import com.larvalabs.redditchat.util.Util;
import models.ChatUser;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;
import play.Logger;
import play.Play;
import play.data.validation.Email;
import play.data.validation.Max;
import play.mvc.Controller;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by matt on 1/3/16.
 */
public class UserManage extends PreloadUserController {

    public static void prefs() {
        ChatUser user = connected();

        render(user);
    }

    public static void save(@Max(100) @Email String email, @Max(100) String status, File profileimage) {
        ChatUser user = connected();
        user.setEmail(Util.clean(email));
        user.setStatusMessage(Util.clean(status));

        if (profileimage != null) {
            if (profileimage.length() > Constants.MAX_PROFILE_IMAGE_SIZE_BYTES) {
                Logger.error("Profile image too large: " + profileimage.length() + " max: " + Constants.MAX_PROFILE_IMAGE_SIZE_BYTES);

                TopAlert alert = new TopAlert(TopAlert.Type.ERROR, "Image file size is too large.", "Please try a smaller file.");
                alert.toFlash(flash);

            } else {
                try {

                    BufferedImage inputImg = ImageIO.read(profileimage);
                    if (inputImg == null) {
                        throw new IOException();
                    }

                    S3Service s3Service = getS3Service();
                    S3Bucket profileBucket = getProfileBucket(s3Service);
                    S3Object picObj = new S3Object(profileBucket, profileimage);
                    String s3Filename = getS3Filename(user, "profile-");
                    Logger.info("S3 filename: " + s3Filename);
                    picObj.setName(s3Filename);
                    s3Service.putObject(profileBucket, picObj);
                    Logger.info("Upload complete.");

                    // Just set the filename for now since image editing isn't complete
                    user.setProfileImageKey(s3Filename);
                    user.save();

                } catch (S3ServiceException e) {
                    TopAlert alert = new TopAlert(TopAlert.Type.ERROR, "Error saving profile image.", "Please try again later.");
                    alert.toFlash(flash);

                    Logger.error(e, "Error saving to S3.");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    TopAlert alert = new TopAlert(TopAlert.Type.ERROR, "Error reading profile image.", "Please make sure you uploaded an image file.");
                    alert.toFlash(flash);

                    e.printStackTrace();
                }

            }
        }

        user.save();

        TopAlert alert = new TopAlert(TopAlert.Type.SUCCESS, "Profile updated.", "Profile information saved.");
        alert.toFlash(flash);

        Logger.info("User " + user.username + " saved.");
        prefs();
    }

    // todo get the file extension right
    public static String getS3Filename(ChatUser user, String filePrefix) {
        return filePrefix + user.getUsername() + ".jpg";
    }

    public static S3Service getS3Service() throws S3ServiceException {
        String awsAccessKey = Play.configuration.getProperty("aws.accesskey");
        String awsSecretKey = Play.configuration.getProperty("aws.secretkey");
        AWSCredentials awsCredentials = new AWSCredentials(awsAccessKey, awsSecretKey);
        return new RestS3Service(awsCredentials);
    }

    public static S3Bucket getProfileBucket(S3Service s3Service) throws S3ServiceException {
        return s3Service.getBucket(Constants.S3BUCKET_PROFILEPICS);
    }

}
