package com.krishna.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;

/**
 * 
 * @author shivangi
 *
 * The File upload service
 */
@Service
public class FileUploadService {

	@Value("${cloud.aws.credentials.accessKey}")
	private String accessKey;

	@Value("${cloud.aws.credentials.secretKey}")
	private String secretKey;

	@Value("${app.awsServices.bucketName}")
	private String bucketName;

	@Value("${cloud.aws.region.static}")
	private String bucketRegion;
	
	@Value("${aws.s3.url}")
	private String awsUrl;

	private final long EXPIRATION_TIME = 1000 * 60 * 60;

	Logger logger=LoggerFactory.getLogger(FileUploadService.class);
	
	// generating pre signed url
	public String generatePresignedUrl(String fileName, String folderName) {
		AmazonS3 s3Client = gets3Client();
		Date expiration = new Date();
		long timestamp = expiration.getTime();
		timestamp += EXPIRATION_TIME;// link expiration time is 1 hour
		expiration.setTime(timestamp);
		GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(
				bucketName + "" + folderName, fileName).withMethod(HttpMethod.GET).withExpiration(expiration);

		URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
		return url.toString();
	}

	// generating pre signed url with original FileName
	public String generatePresignedUrl(String fileName, String folderName, String originalFileName) {
		AmazonS3 s3Client = gets3Client();
		Date expiration = new Date();
		long timestamp = expiration.getTime();
		timestamp += EXPIRATION_TIME;// link expiration time is 1 hour
		expiration.setTime(timestamp);
		GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(
				bucketName + "" + folderName, fileName).withMethod(HttpMethod.GET).withExpiration(expiration);

		ResponseHeaderOverrides responseHeaders = new ResponseHeaderOverrides();
		responseHeaders.setContentDisposition("attachment; filename =\"" + originalFileName + "\"");
		generatePresignedUrlRequest.setResponseHeaders(responseHeaders);

		URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
		return url.toString();
	}

	// obtain s3 client object
	public AmazonS3 gets3Client() {

		return AmazonS3Client.builder()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(awsUrl, bucketRegion))
                .withPathStyleAccessEnabled(true)
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .build();
	}

	// to upload file on s3
	public Map<String, Object> uploadFileOnS3Bucket(String imageData, String imageName, String folderName)
			throws Exception {
		String updatedFileName = null;
		String originalFileName = imageName;
		int dot = originalFileName.lastIndexOf(".");
		String extension = (dot == -1) ? "" : originalFileName.substring(dot + 1);
		updatedFileName = UUID.randomUUID().toString() + "." + extension;
		final byte[] bI = org.apache.commons.codec.binary.Base64
				.decodeBase64((imageData.substring(imageData.indexOf(",") + 1)).getBytes());
		Map<String, Object> result = new HashMap<>();
		try {
			InputStream fis = new ByteArrayInputStream(bI);
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentLength(bI.length);
			AmazonS3 s3Client = gets3Client();
			s3Client.putObject(new PutObjectRequest(bucketName + "" + folderName, updatedFileName, fis, metadata)
					.withCannedAcl(CannedAccessControlList.Private));
			fis.close();
		} catch (Exception ex) {
			logger.info(" " + ex);
		}
		result.put("imagePath", updatedFileName);
		result.put("originalFileName", originalFileName);
		return result;
	}

	// deleting document based on identifier in aws
	public boolean deleteFileFroms3(String folderName, String docIdentifier) throws IOException {
		AmazonS3 s3Client = gets3Client();
		s3Client.deleteObject(new DeleteObjectRequest(bucketName + "" + folderName, docIdentifier));
		return true;
	}
	
	/**
	 * Upload Ticket File in S3 bucket
	 *
	 * @param fileData
	 * @param imageName
	 * @param folderName
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> uploadFileOnS3Bucket(byte[] fileData, String fileName, String folderName)
			throws Exception {
		String updatedFileName = null;
		String originalFileName = fileName;
		int dot = originalFileName.lastIndexOf(".");
		String extension = (dot == -1) ? "" : originalFileName.substring(dot + 1);
		updatedFileName = UUID.randomUUID().toString() + "." + extension;

		Map<String, Object> result = new HashMap<>();
		try {
			InputStream fis = new ByteArrayInputStream(fileData);
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentLength(fileData.length);
			AmazonS3 s3Client = gets3Client();
			s3Client.putObject(new PutObjectRequest(bucketName + "" + folderName, updatedFileName, fis, metadata)
					.withCannedAcl(CannedAccessControlList.Private));
			fis.close();
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		result.put("imagePath", updatedFileName);
		result.put("originalFileName", originalFileName);
		return result;
	}

}
