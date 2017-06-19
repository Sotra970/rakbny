package com.rakbny.Utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.rakbny.R;
import com.rakbny.data.app.AppController;
import com.rakbny.data.app.Config;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sotra on 11/1/2016.
 */
public class Utils {

   public static File createPdf(String pdf_name, String mSubjectEditText, String mBodyEditText , Context context) throws FileNotFoundException, DocumentException {

       File pdfFolder = new File(Environment.getExternalStorageDirectory(), "rakbny_pdfs");
       if (!pdfFolder.exists()) {
           pdfFolder.mkdir();
           Log.i("create pdf ", "Pdf Directory created");
       }

       //Create time stamp
       Date date = new Date() ;
       String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(date);
       File myFile = new File(pdfFolder+"/"+pdf_name+"_" + timeStamp + ".pdf");

       OutputStream output = new FileOutputStream(myFile);

       //Step 1
       Document document = new Document();
       //Step 2
       PdfWriter.getInstance(document, output);

       //Step 3
       document.open();

       //Step 4 Add content
       document.add(new Paragraph(mSubjectEditText));
       document.add(new Paragraph(mBodyEditText));

       //Step 5: Close the document
       document.close();
       return  myFile ;
   }



}
