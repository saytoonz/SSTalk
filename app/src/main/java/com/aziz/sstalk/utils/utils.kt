package com.aziz.sstalk.utils

import android.Manifest
import android.animation.Animator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.*
import android.provider.ContactsContract
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.FileProvider
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.aziz.sstalk.BuildConfig
import com.aziz.sstalk.R
import com.aziz.sstalk.models.Models
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern


object utils {


    object constants {

        val FILE_TYPE_IMAGE = "image"
        val FILE_TYPE_LOCATION = "location"
        val FILE_TYPE_VIDEO = "video"
        val KEY_IMG_PATH = "path"
        val KEY_CAPTION = "caption"
        val KEY_LOCAL_PATH = "local_path"

        val KEY_MSG_MODEL = "msg_model"

        val KEY_LATITUDE = "lat"
        val KEY_LONGITUDE = "lng"
        val KEY_ADDRESS = "address"

        val KEY_NAME = "name"

        val IS_FOR_SINGLE_FILE = "isSingleFile"
        val URI_AUTHORITY = "com.mvc.imagepicker.provider"

        val KEY_FILE_TYPE = "type"
        val debugUserID = "user---2"
        val debugUserID2 = "user---1"

        val REGEX_PATTERN_PHONE = "^(\\+\\d{1,2}\\s)?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}\$"


    }


    fun toast(context: Context?, message: CharSequence) =
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

    fun longToast(context: Context?, message: CharSequence) =
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()


    fun getFormattedTenDigitNumber(number:String ) : String {
        var out = Pattern.compile("[^0-9]").matcher(number).replaceAll("")

        if(out.length>10){
            out = number.substring(number.length - 10)
        }

        return out
    }



    fun getContactList(context: Context?) : MutableList<Models.Contact>{

        val numberList:MutableList<Models.Contact> = mutableListOf()
        val cursor = context!!.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null , null, null , null)

        while(cursor!!.moveToNext()){

            var name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            var number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

            var pic = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI))

            number = utils.getFormattedTenDigitNumber(number)

            if(pic == null)
                pic = ""

            if(name == null)
                name = ""



            var isDuplicate = false
            for(item in numberList){
                if(item.number == number ) {
                    isDuplicate = true
                    break

                }

            }

            if(FirebaseUtils.isLoggedIn()){
                isDuplicate = FirebaseAuth.getInstance().currentUser!!.phoneNumber == number
            }

            if(!isDuplicate)
                numberList.add(Models.Contact(name, number , pic))


        }

        cursor.close()
        return numberList

    }


    fun getNameFromPhone(){

    }

    fun hasContactPermission(context: Context) = (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)

    fun hasStoragePermission(context: Context) = (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            && (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)

    fun getLocalTime(timeInMillis: Long): String{

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMillis
        val sdf = SimpleDateFormat("hh:mm a")

        sdf.timeZone = TimeZone.getDefault()

        return sdf.format(calendar.time)
    }

    fun getLocalDate(timeInMillis: Long): String{

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMillis
        val sdf = SimpleDateFormat("dd MMM yy")

        sdf.timeZone = TimeZone.getDefault()

        return sdf.format(calendar.time)
    }

    fun getUtcTimeFromMillis(timeInMillis:Long) : String{

        val calendar = Calendar.getInstance()
        calendar.setTimeInMillis(timeInMillis)
        val sdf = SimpleDateFormat("hh:mm a")

        sdf.timeZone = TimeZone.getDefault()

        return sdf.format(calendar.time)
    }


    fun getByteArrayFromBitmap(bitmap: Bitmap) : ByteArray {
        val bout = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG,100, bout)
        return bout.toByteArray()
    }

    fun getBitmapFromByteArray(byteArray: ByteArray) : Bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)


    fun setEnterRevealEffect(view:View): Animator? {


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            view.visibility = View.VISIBLE
            return null
        }


            // get the center for the clipping circle
        val cx = (view.left )
        val cy = (view.top + view.bottom)

        // get the final radius for the clipping circle
        val dx = Math.max(cx, view.width - cx).toDouble()
        val dy = Math.max(cy, view.height - cy).toDouble()
        val finalRadius =  Math.hypot(dx, dy).toFloat()

        val animator =
            ViewAnimationUtils.createCircularReveal(view, cx, cy, 0f, finalRadius)
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = 800

        animator.addListener(object : Animator.AnimatorListener{
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
                view.visibility = View.VISIBLE
            }

        })
        animator.start()

        return animator

    }


    fun setExitRevealEffect(view:View): Animator? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            view.visibility = View.GONE
            return null
        }

        val cx = ( view.right + view.left)
        val cy = (view.top + view.bottom)

// get the final radius for the clipping circle
        val dx = Math.max(cx, view.width + cx)
        val dy = Math.max(cy, view.height + cy)
        val finalRadius = Math.hypot(dx.toDouble(), dy.toDouble()).toFloat()

        val animator =
            ViewAnimationUtils.createCircularReveal(view, cx, cy, finalRadius, 0f)
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = 1000

        animator.addListener(object : Animator.AnimatorListener{
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                view.visibility = View.GONE
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

        })

        animator.start()


        return animator


    }


    fun saveBitmapToSent(context: Context?, bitmap: Bitmap, messageIdForName:String):String{

        val fileName = "$messageIdForName.jpg"

        val path = Environment.getExternalStorageDirectory().toString()+"/"+ context!!.getString(R.string.app_name).toString()+"" +
                "/Images/Sent/"

        if(!File(path).exists())
            File(path).mkdirs()

        val file = File(path, fileName)

        try {

                val fout = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fout)
                Log.d("utils", "saveBitmap: file saved to ${file.path}")

        }
        catch (e:Exception){
            Log.d("utils", "saveBitmap: File not found")
        }

        return file.path
    }


    fun getProfilePicPath(context: Context):String = Environment.getExternalStorageDirectory().toString()+"/"+ context.getString(R.string.app_name).toString()+"" +
            "/ProfilePics/"

    fun saveBitmapToProfileFolder(context: Context?, bitmap: Bitmap, messageIdForName:String):String{

        val fileName = "$messageIdForName.jpg"

        val path = Environment.getExternalStorageDirectory().toString()+"/"+ context!!.getString(R.string.app_name).toString()+"" +
                "/ProfilePics/"

        if(!File(path).exists())
            File(path).mkdirs()

        val file = File(path, fileName)

        try {

            val fout = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fout)
            Log.d("utils", "saveBitmap: file saved to ${file.path}")


            val values = ContentValues(3)
            values.put(MediaStore.Video.Media.TITLE, messageIdForName)
            values.put(MediaStore.Video.Media.MIME_TYPE, "image/*")
            values.put(MediaStore.Video.Media.DATA, file.absolutePath)
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        }
        catch (e:Exception){
            Log.d("utils", "saveBitmap: File not found")
        }

        return file.path
    }

    fun saveBitmapToReceived(context: Context?, bitmap: Bitmap, messageIdForName:String):String{

        val fileName = "$messageIdForName.jpg"

        val path = Environment.getExternalStorageDirectory().toString()+"/"+ context!!.getString(R.string.app_name).toString()+"" +
                "/Images/Received/"

        if(!File(path).exists())
            File(path).mkdirs()

        val file = File(path, fileName)

        try {

            val fout = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fout)
            Log.d("utils", "saveBitmap: file saved to ${file.path}")


            val values = ContentValues(3)
            values.put(MediaStore.Video.Media.TITLE, messageIdForName)
            values.put(MediaStore.Video.Media.MIME_TYPE, "image/*")
            values.put(MediaStore.Video.Media.DATA, file.absolutePath)
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        }
        catch (e:Exception){
            Log.d("utils", "saveBitmap: File not found")
        }

        return file.path
    }

    fun getVideoLength(context: Context?, videoFilePath:String):String{
        try {

            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, utils.getUriFromFile(context,  File(videoFilePath)))
            val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val timeInMillisec = time.toLong()

            retriever.release()
//            return String.format(
//                "%d:%d",
//                TimeUnit.MILLISECONDS.toMinutes(timeInMillisec),
//                TimeUnit.MILLISECONDS.toSeconds(timeInMillisec) -
//                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeInMillisec))
//            )

            return getDurationString(timeInMillisec)
        }
        catch (e:Exception){return ""}

    }


    fun startVideoIntent(context: Context, videoPath: String){
      try{
            val videoIntent = Intent(Intent.ACTION_VIEW)
            val uri =utils.getUriFromFile(context,  File(videoPath))
            videoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            videoIntent.setDataAndType(uri, "video/*")
            context.startActivity(videoIntent)
        }
        catch (e:Exception){
            utils.toast(context, e.message.toString())
        }
    }


    fun getDurationString(duration: Long): String {
        //        long days = duration / (1000 * 60 * 60 * 24);
        val hours = duration % (1000 * 60 * 60 * 24) / (1000 * 60 * 60)
        val minutes = duration % (1000 * 60 * 60) / (1000 * 60)
        val seconds = duration % (1000 * 60) / 1000

        val hourStr = if (hours < 10) "0$hours" else hours.toString() + ""
        val minuteStr = if (minutes < 10) "0$minutes" else minutes.toString() + ""
        val secondStr = if (seconds < 10) "0$seconds" else seconds.toString() + ""

        return if (hours != 0L) {
            "$hourStr:$minuteStr:$secondStr"
        } else {
            "$minuteStr:$secondStr"
        }
    }


   private class getThumbFromURL(private val imageView: ImageView, private val videoPath: String) : AsyncTask <Void, Void, Void>(){
       var bitmap: Bitmap? = null
        val mediaMetadataRetriever:MediaMetadataRetriever? = MediaMetadataRetriever()


        override fun doInBackground(vararg params: Void?): Void? {


            try
            {
                if (Build.VERSION.SDK_INT >= 14)
                    mediaMetadataRetriever!!.setDataSource(videoPath, HashMap<String, String>())
                else
                    mediaMetadataRetriever!!.setDataSource(videoPath)
                //   mediaMetadataRetriever.setDataSource(videoPath);
                bitmap = mediaMetadataRetriever.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST);
            }
            catch (e:Exception)
            {
                e.printStackTrace()
            }
            finally
            {
                mediaMetadataRetriever!!.release()
            }

            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            mediaMetadataRetriever!!.release()
            imageView.setImageBitmap(bitmap)

        }

    }



    fun setVideoThumbnailFromWebAsync(videoPath: String, imageView: ImageView){
        getThumbFromURL(imageView, videoPath)
            .execute()
    }


    private class loadImageUsingPicassoFromFile(private val file: File,private val imageView: ImageView) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {


            Handler(Looper.getMainLooper())
                .post {

            Picasso.get()
                .load(file)
                .tag(file.path)
                .fit()
                .centerCrop()
                //.resize(600,400)
                .error(R.drawable.error_placeholder2)
                .placeholder(R.drawable.placeholder_image)
                .into(imageView)

                }

                    return null
        }

    }


    private class loadImageUsingPicassoFromWeb(private val url: String,private val imageView: ImageView) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            Picasso.get()
                .load(url)
                .tag(url)
                .fit()
                .centerCrop()
                //.resize(600,400)
                .error(R.drawable.error_placeholder2)
                .placeholder(R.drawable.placeholder_image)
                .into(imageView)
            return null
        }

    }


    fun loadFileUsingPicassoFromWeb(url: String, imageView: ImageView){
        loadImageUsingPicassoFromWeb(url,imageView).execute()
    }


    fun loadUsingPicassoFromFile(file: File, imageView: ImageView){
        loadImageUsingPicassoFromFile(file,imageView).execute()
    }


     fun getVideoThumbnailFromWeb(videoPath:String, imageView: ImageView): Bitmap?
    {
    var bitmap: Bitmap? = null
    var mediaMetadataRetriever:MediaMetadataRetriever? = null



    try
    {
        mediaMetadataRetriever = MediaMetadataRetriever()
        if (Build.VERSION.SDK_INT >= 14)
            mediaMetadataRetriever.setDataSource(videoPath, HashMap<String, String>())
            else
                mediaMetadataRetriever.setDataSource(videoPath)
     //   mediaMetadataRetriever.setDataSource(videoPath);
        bitmap = mediaMetadataRetriever.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST);
    }
    catch (e:Exception)
    {
        e.printStackTrace()
    }
    finally
    {
        mediaMetadataRetriever!!.release()
    }
    return bitmap
}

    fun loadVideoThumbnailFromLocalAsync(context: Context, imageView: ImageView, path:String){
        val task = @SuppressLint("StaticFieldLeak")
        object : AsyncTask<Void, Void, Void>() {

            override fun onPostExecute(result: Void?) {
                super.onPostExecute(result)
                this.cancel(true)
            }

            override fun doInBackground(vararg params: Void?): Void? {
                try{
                    imageView.setImageBitmap(ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MICRO_KIND))
                }
                catch (e:Exception) {}
                return null
            }

        }
        task.execute()
    }


    fun getVideoFile(context: Context?, messageIdForName: String):File {

        val fileName = "$messageIdForName.mp4"

        val path =
            Environment.getExternalStorageDirectory().toString() + "/" + context!!.getString(R.string.app_name).toString() + "" +
                    "/Video/Received/"

        if (!File(path).exists())
            File(path).mkdirs()

        val file = File(path, fileName)
        return file
    }

    fun saveVideo(context: Context?, fileBytes: ByteArray, messageIdForName:String):String{

        val fileName = "$messageIdForName.mp4"

        val path = Environment.getExternalStorageDirectory().toString()+"/"+ context!!.getString(R.string.app_name).toString()+"" +
                "/Video/Received/"

        if(!File(path).exists())
            File(path).mkdirs()

        val file = File(path, fileName)

        try {

            val fout = FileOutputStream(file)
            fout.write(fileBytes)
          //  bitmap.compress(Bitmap.CompressFormat.PNG, 100, fout)
            Log.d("utils", "saveVideo: file saved to ${file.path}")

            addVideoToMediaStore(context, messageIdForName, file)
        }
        catch (e:Exception){
            Log.d("utils", "saveVideo: File not found")
        }

        return file.path
    }


    private fun addVideoToMediaStore(context:Context, messageIdForName: String, file: File){
        val values = ContentValues(3)
        values.put(MediaStore.Video.Media.TITLE, messageIdForName)
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        values.put(MediaStore.Video.Media.DATA, file.absolutePath)

        //getting video length
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, utils.getUriFromFile(context, file))
        val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val timeInMillisec = time.toLong()

        retriever.release()

        values.put(MediaStore.Video.Media.DURATION, timeInMillisec )
        context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)

    }


    fun highlightTextView(textView: TextView, highlightedText:String, color:Int) {

        try {
            val text = textView.text.toString().toLowerCase()

            val startIndex = text.indexOf(highlightedText.toLowerCase())
            val endIndex = startIndex + highlightedText.length

            val spannableString = SpannableString(text)
            spannableString.setSpan(
                BackgroundColorSpan(color), startIndex,
                endIndex, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            )


            textView.text = spannableString
        }
        catch (e:Exception){}
    }


    fun hideSoftKeyboard(activity: Activity) {
        try {
            val inputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(activity.currentFocus.windowToken, 0)
        } catch (e:Exception) {
            e.printStackTrace()
        }
    }



    fun getUriFromFile(context: Context?, file:File) : Uri {

        var uri = Uri.fromFile(file)

        if(Build.VERSION.SDK_INT>= 24)
            uri = FileProvider.getUriForFile(context!!, constants.URI_AUTHORITY, file)

        return uri

    }
}