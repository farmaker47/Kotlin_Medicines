package com.george.kotlin_medicines

import android.Manifest
import android.app.DownloadManager
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class KotlinMainActivity : AppCompatActivity() {

    private var nameSpcPdf = "recipe_spc.pdf"
    private var downloadID: Long? = null
    private val PERMISSIONS =
        Manifest.permission.READ_EXTERNAL_STORAGE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        //lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)

        /*fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }*/

        //setting the Up button
        val navController = this.findNavController(R.id.nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController)

        //Register receiver
        registerReceiver(onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.nav_host_fragment)
        return navController.navigateUp()
    }

    private val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //Fetching the download id received with the broadcast
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            //Checking if the received broadcast is for our enqueued download by matching download id
            if (downloadID == id) {
                viewPdf(nameSpcPdf)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(onDownloadComplete)
    }

    //Check permissions
    private fun hasPermissions(
        context: Context?,
        vararg permissions: String
    ): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

    fun setNameOfPdf(name: String) {
        nameSpcPdf = name
    }

    fun viewPdf(name: String) {
        if (!hasPermissions(
                this,
                PERMISSIONS
            )
        ) {
            Toast.makeText(
                applicationContext,
                "You don't have read access!",
                Toast.LENGTH_LONG
            ).show()
        } else {
            if (name.endsWith(".pdf")) {
                val pdfFile = File(getExternalFilesDir(null), name)
                val path = FileProvider.getUriForFile(
                    this,
                    BuildConfig.APPLICATION_ID + ".fileprovider",
                    pdfFile
                )
                val pdfIntent = Intent(Intent.ACTION_VIEW)
                pdfIntent.setDataAndType(path, "application/pdf")
                pdfIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                try {
                    startActivity(pdfIntent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(this, "No Application available to view PDF", Toast.LENGTH_SHORT)
                        .show()
                }
            } else if (name.endsWith(".doc") || name.endsWith(".docx")) {
                val pdfFile = File(getExternalFilesDir(null), name)
                val path = FileProvider.getUriForFile(
                    this,
                    BuildConfig.APPLICATION_ID + ".fileprovider",
                    pdfFile
                )

                val pdfIntent = Intent(Intent.ACTION_VIEW)
                pdfIntent.setDataAndType(path, "text/plain")
                pdfIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                /*Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("/");
                String[] mimetypes = {"application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/msword"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);*/
                try {
                    startActivity(pdfIntent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(
                            this,
                            "No Application available to view Word files",
                            Toast.LENGTH_SHORT
                        )
                        .show()
                }
            }
        }
    }

    fun beginDownload(
        url: String?,
        cookiesBrowser: String?,
        namePdf: String?
    ) {
        val file = File(getExternalFilesDir(null), namePdf)
        if (file.exists()) {
            file.delete()
        }
        Log.v("CookiesDetails", cookiesBrowser)

        //Create a DownloadManager.Request with all the information necessary to start the download
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("SPC File") // Title of the Download Notification
            .setDescription("Downloading") // Description of the Download Notification
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE) // Visibility of the download Notification
            .setDestinationUri(Uri.fromFile(file)) // Uri of the destination file
            .setAllowedOverMetered(true) // Set if download is allowed on Mobile network
            .setAllowedOverRoaming(true)
        request.addRequestHeader("cookie", cookiesBrowser)
        /*request.addRequestHeader("User-Agent", cookiesBrowser);*/
        val downloadManager =
            getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request) // enqueue puts the download request in the queue.
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
