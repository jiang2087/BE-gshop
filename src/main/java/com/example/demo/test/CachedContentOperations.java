package com.example.demo.test;

import com.example.demo.utils.Constant;
import com.google.genai.Client;
import com.google.genai.types.*;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.time.Duration;
import java.time.Instant;

public class CachedContentOperations {

    private static @Nullable Part fetchPdfPart(String pdfUrl){
        String mimeType = "application/pdf";
        byte[] pdfData = null;
        System.out.println("Attempting to download PDF from  " + pdfUrl);

        try {
            URL url = new URL(pdfUrl);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);
            try(InputStream inputStream = connection.getInputStream()){
                pdfData = inputStream.readAllBytes();
            }
            if(pdfData.length > 0){
                System.out.println("Successfully downloaded " + pdfData.length + " bytes.");
                return  Part.fromBytes(pdfData, mimeType);
            }else{
                System.err.println("Failed to download PDF data or the file was empty.");
                return null;
            }

        } catch (RuntimeException e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        final String modelId;
        if(args.length != 0){
            modelId = args[0];
        }else{
            modelId = Constant.GEMINI_MODEL_NAME;
        }

        Client client = new Client();
        if(client.vertexAI()){
            System.out.println("Using vertex AI");
        }else{
            System.out.println("Using Gemini Developer API");
        }
        Content content = Content.fromParts(
                fetchPdfPart(
                        "https://storage.googleapis.com/cloud-samples-data/generative-ai/pdf/2403.05530.pdf")
        );
        CreateCachedContentConfig config =
                CreateCachedContentConfig.builder()
                        .systemInstruction(Content.fromParts(Part.fromText("summarize the pdf")))
                        .expireTime(Instant.now().plus(Duration.ofHours(1)))
                        .contents(content)
                        .build();
        CachedContent cachedContent = client.caches.create(modelId, config);
        System.out.println("Created cached content: " + cachedContent);

        // Get the cached content by name
        CachedContent cachedContent1 = client.caches.get(cachedContent.name().get(), null);
        System.out.println("Get cached content: " + cachedContent1);

        CachedContent cachedContent2 = client.caches.update(
                cachedContent.name().get(),
                UpdateCachedContentConfig.builder().ttl(Duration.ofMinutes(10)).build()
        );
        // list all cached contents
        System.out.println("List cached contents resrouce names: ");
        for(CachedContent cachedContent3 :
        client.caches.list(ListCachedContentsConfig.builder().pageSize(5).build())
        ){
            System.out.println(cachedContent3.name().get());
        }

        //Delete the cached content
        DeleteCachedContentResponse unused = client.caches.delete(cachedContent.name().get(), null);
        System.out.println(
                "Delete cached content: " + cachedContent.name().get()
        );
    }

}

