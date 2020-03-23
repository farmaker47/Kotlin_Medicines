package com.george.kotlin_medicines

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.JsonReader
import android.util.JsonToken
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.transition.TransitionInflater
import com.george.kotlin_medicines.databinding.ActivityScrollingDetailsFragmentBinding
import com.george.view_models.PackageFragmentViewModel
import com.squareup.picasso.Picasso
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.IOException
import java.io.StringReader
import java.util.*

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val NAME_OF_MEDICINES = "name_of_medicines"
private val PERMISSIONS = arrayOf(
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.READ_EXTERNAL_STORAGE
)
private const val URL_TO_SERVE = "https://services.eof.gr/drugsearch/SearchName.iface"
private const val URL_FOR_PDFs = "https://services.eof.gr"


/**
 * A simple [Fragment] subclass.
 * Use the [PackageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PackageFragment : Fragment() {
    private var medicine_name: String? = null
    private var param2: String? = null
    private lateinit var binding: ActivityScrollingDetailsFragmentBinding
    private var cookieStringStripped: String? = null
    private lateinit var packageViewModel: PackageFragmentViewModel
    private val args: PackageFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            medicine_name = it.getString(NAME_OF_MEDICINES)
            param2 = it.getString(ARG_PARAM2)
        }

        //shared element transition
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            sharedElementEnterTransition =
                TransitionInflater.from(context)
                    .inflateTransition(R.transition.move)
            sharedElementReturnTransition =
                TransitionInflater.from(context)
                    .inflateTransition(R.transition.move)

        }
        /*val handler = Handler()
        handler.postDelayed(
            {

            },
            10
        )*/


    }

    override fun onResume() {
        super.onResume()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.activity_scrolling_details_fragment,
            container,
            false
        )

        //set title
        activity?.title = getString(R.string.titleDetails)

        //get args
        /*val imageUri = args.uri
        Log.e("URI", imageUri)
        binding.dummyImageViewShared.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                transitionName = imageUri
            }
        }*/

        //set view model
        packageViewModel = ViewModelProvider(this).get(PackageFragmentViewModel::class.java)

        //set text
        binding.titleTextViewGray.text = medicine_name
        //Load dummy image
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Picasso.get().load(R.drawable.recipe_icon).into(binding.detailActivityImage)
        }

        //load url and fetch info if savedInstanceState is null
        if (savedInstanceState == null) {
            Log.e("NULL","NULL")
            binding.webViewPackage.webViewClient = object : WebViewClient() {

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    binding.progressBarPackage.visibility = View.VISIBLE
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    binding.progressBarPackage.visibility = View.GONE
                    fetchAllInfo()
                    cookieStringStripped = CookieManager.getInstance().getCookie(url)
                    packageViewModel.setStringCookies(cookieStringStripped!!)
                    Log.i("COOKIES", cookieStringStripped)
                }
            }

            //Enable Javascript
            binding.webViewPackage.settings.javaScriptEnabled = true
            //Clear All and load url
            //Clear All and load url
            binding.webViewPackage.loadUrl(URL_TO_SERVE)
        } else {
            parseAllInfo(packageViewModel.stringOfHtml)
            cookieStringStripped = packageViewModel.stringCookies
        }

        //Request permissions to read and write
        ActivityCompat.requestPermissions(activity!!, PERMISSIONS, 112)
        return binding.root
    }

    override fun onPause() {
        super.onPause()
        binding.progressBarPackage.visibility = View.GONE
    }

    private fun fetchAllInfo() {
        binding.webViewPackage.evaluateJavascript(
            "(function() { return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); })();",
            ValueCallback<String?> { html ->
                val reader =
                    JsonReader(StringReader(html))
                reader.isLenient = true
                try {
                    if (reader.peek() == JsonToken.STRING) {
                        val domStr = reader.nextString()
                        domStr?.let { parseAllInfo(it) }
                        packageViewModel.setStringOfHtml(domStr)
                    }
                } catch (e: IOException) {
                    // handle exception
                }
            })
    }

    private fun parseAllInfo(domStr: String) {
        val doc = Jsoup.parse(domStr)
        if (checkElement(doc.select("input[id=form1:btnBack]").first())) {

            //Kodikos EOF
            if (checkElement(doc.select("span[id=form1:txtDRUGID]").first())) {
                val kodikosEof =
                    doc.select("span[id=form1:txtDRUGID]").first()
                binding.farmakMorfi.text = kodikosEof.text()
            }

            //Nomiko kathestos
            if (checkElement(doc.select("span[id=form1:txtLESTATUS]").first())) {
                val nomikoKAthestos =
                    doc.select("span[id=form1:txtLESTATUS]").first()
                binding.nomikoKathestos.text = nomikoKAthestos.text()
            }

            //Morfi
            if (checkElement(doc.select("span[id=form1:tblDrform:0:txtformcode]").first())) {
                val morfi =
                    doc.select("span[id=form1:tblDrform:0:txtformcode]").first()
                binding.morfiEofTextView.text = morfi.text()
            }

            //Periektikotita
            if (checkElement(doc.select("span[id=form1:tblDrform:0:txtStrength]").first())) {
                val periektikotita =
                    doc.select("span[id=form1:tblDrform:0:txtStrength]").first()
                binding.periektikotita.text = periektikotita.text()
            }

            //Odos Xorigisis
            if (checkElement(doc.select("span[id=form1:tblDRROUTE:0:txtDrroute]").first())) {
                val odos =
                    doc.select("span[id=form1:tblDRROUTE:0:txtDrroute]").first()
                binding.odosXorigisisTextView.text = odos.text()
            }

            //Kodikos ATC
            if (checkElement(doc.select("span[id=form1:tblATC:0:txtATCcode]").first())) {
                val kodikos_atc =
                    doc.select("span[id=form1:tblATC:0:txtATCcode]").first()
                binding.kodikosAtcTextView.text = kodikos_atc.text()
            }

            //Perigrafi ATC
            if (checkElement(doc.select("span[id=form1:tblATC:0:txtATCDESCR]").first())) {
                val perigrafi_atc =
                    doc.select("span[id=form1:tblATC:0:txtATCDESCR]").first()
                binding.perigrafiAtcTextView.text = perigrafi_atc.text()
            }

            //Sistatika
            if (checkElement(doc.select("table[id=form1:tblActiveIngredients]").first())) {
                val arrayForTextView =
                    ArrayList<String>()
                val row =
                    doc.select("table[id=form1:tblActiveIngredients]").select(".iceDatTblCol2")
                        .select("span[id$=SUNAME]")
                if (row != null) {
                    for (element in row) {
                        val text = element.text()
                        arrayForTextView.add(text)
                    }
                }
                for (i in arrayForTextView.indices) {
                    //Creating a view
                    val linearLayout = LinearLayout(context)
                    val paramsLinear = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    paramsLinear.setMargins(0, 0, 0, 24)
                    linearLayout.layoutParams = paramsLinear
                    linearLayout.orientation = LinearLayout.VERTICAL

                    //text for description
                    val ingredient2 = TextView(context)
                    ingredient2.setText(R.string.wait_String)
                    val params2 = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params2.setMargins(32, 8, 8, 16)
                    ingredient2.layoutParams = params2
                    ingredient2.textSize = 16f
                    /*ingredient.setPadding(4, 4, 4, 4);*/ingredient2.setTextColor(Color.GRAY)
                    ingredient2.visibility = View.GONE
                    /*final String resultString2 = pingAndFetchText(DRUGS_CA, arrayForTextView.get(i));*/

                    //text for name
                    val ingredient = TextView(context)
                    ingredient.text = arrayForTextView[i]
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(32, 8, 8, 16)
                    ingredient.layoutParams = params
                    ingredient.textSize = 18f
                    ingredient.setPadding(4, 4, 4, 4)
                    ingredient.paintFlags = ingredient.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                    ingredient.setTextColor(Color.BLUE)
                    //ripple effect
                    val outValue = TypedValue()
                    context!!.theme
                        .resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
                    ingredient.setBackgroundResource(outValue.resourceId)
                    ingredient
                        .setOnClickListener {
                            //listener for drastiki

                            /*mListener.onFragmentInteractionPackage(
                                arrayForTextView[i],
                                arrayForTextView[i],
                                arrayForTextView[i]
                            )*/

                            val bundle = Bundle()
                            bundle.putString(NAME_OF_MEDICINES, arrayForTextView[i])
                            /*val bundle: Bundle = ActivityOptionsCompat
                                .makeSceneTransitionAnimation(
                                    ,
                                    sharedImage,
                                    sharedImage!!.transitionName
                                ).toBundle()*/
                            findNavController().navigate(
                                R.id.action_packageFragment_to_ingredientFragment,
                                bundle
                            )
                        }
                    linearLayout.addView(ingredient)
                    linearLayout.addView(ingredient2)
                    binding.linearSistatika.addView(linearLayout)
                }
            }

            //Onomasia etairias
            if (checkElement(
                    doc.select("td[id=form1:panelGrid6-0-1]").select("span[id=form1:txtName]")
                        .first()
                )
            ) {
                val onomasia_etairias =
                    doc.select("td[id=form1:panelGrid6-0-1]").select("span[id=form1:txtName]")
                        .first()
                binding.onomasiaEtairiasTextView.text = onomasia_etairias.text()
            }

            //Address
            if (checkElement(
                    doc.select("td[id=form1:panelGrid6-2-1]").select("span[id=form1:txtAddress]")
                        .first()
                )
            ) {
                val address_etairias =
                    doc.select("td[id=form1:panelGrid6-2-1]").select("span[id=form1:txtAddress]")
                        .first()
                binding.addressEtairiasTextView.text = address_etairias.text()
            }

            //tilefono
            if (checkElement(
                    doc.select("td[id=form1:panelGrid6-3-1]").select("span[id=form1:txtPhone]")
                        .first()
                )
            ) {
                val tilefono_etairias =
                    doc.select("td[id=form1:panelGrid6-3-1]").select("span[id=form1:txtPhone]")
                        .first()
                binding.tilefonoEtairiasTextView.text = tilefono_etairias.text()
            }

            //Fax
            if (checkElement(
                    doc.select("td[id=form1:panelGrid6-4-1]").select("span[id=form1:txtFax]")
                        .first()
                )
            ) {
                val fax_etairias =
                    doc.select("td[id=form1:panelGrid6-4-1]").select("span[id=form1:txtFax]")
                        .first()
                binding.faxEtairiasTextView.text = fax_etairias.text()
            }

            //Mail
            if (checkElement(
                    doc.select("td[id=form1:panelGrid6-5-1]").select("span[id=form1:txtEmail]")
                        .first()
                )
            ) {
                val mail_etairias =
                    doc.select("td[id=form1:panelGrid6-5-1]").select("span[id=form1:txtEmail]")
                        .first()
                binding.mailEtairiasTextView.text = mail_etairias.text()
            }

            //Perilipsi xaraktiristikon
            if (checkElement(
                    doc.select("div[id=form1:orDrugSPC_cont]").select(".iceOutLnk").first()
                )
            ) {
                val perilipsi =
                    doc.select("div[id=form1:orDrugSPC_cont]").select(".iceOutLnk").first()
                binding.perilipsiXaraktiristikonTextView.text = perilipsi.text()
                val perilipsiPdf =
                    doc.select("div[id=form1:orDrugSPC_cont]").select("a[href]").first()
                binding.perilipsiXaraktiristikonTextView.setOnClickListener(View.OnClickListener { /*
                                    webView.loadUrl("javascript:(function(){l=document.getElementById('form1:orDrugSPC_cont');e=document.createEvent('HTMLEvents');e.initEvent('click',true,true);l.dispatchEvent(e);})()");
            */

                    /*Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse(URL_FOR_PDFs + perilipsiPdf.attr("href"))); // only used based on your example.
                                    String title = "Select a browser";
                                    // Create intent to show the chooser dialog
                                    Intent chooser = Intent.createChooser(intent, title);
                                    // Verify the original intent will resolve to at least one activity
                                    if (intent.resolveActivity(context.getPackageManager()) != null) {
                                        startActivity(chooser);
                                    }*/

                    /*webView.setWebViewClient(new WebViewClient() {
                                        public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                            String url2 = "https://services.eof.gr/";
                                            // all links  with in ur site will be open inside the webview
                                            //links that start ur domain example(http://www.example.com/)
                                            if (url != null && url.startsWith(url2)) {
                                                return false;
                                            }
                                            // all links that points outside the site will be open in a normal android browser
                                            else {
                                                view.getContext().startActivity(
                                                        new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                                                return true;
                                            }
                                        }
                                    });
                                    webView.loadUrl("https://services.eof.gr/drugsearch/SearchName.iface");

                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        public void run() {
                                            String urlString = URL_FOR_PDFs + perilipsiPdf.attr("href");
                                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            intent.setPackage("com.android.chrome");
                                            if (intent.resolveActivity(context.getPackageManager()) != null) {
                                                startActivity(intent);
                                            } else {
                                                Toast.makeText(context, R.string.installChrome, Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    }, 2000);*/

                    /*Intent myWebLink = new Intent(Intent.ACTION_VIEW);
                                            myWebLink.setData(Uri.parse(URL_FOR_PDFs + perilipsiPdf.attr("href")));
                                            if (myWebLink.resolveActivity(context.getPackageManager()) != null) {
                                                startActivity(myWebLink);
                                            }*/
                    if (perilipsi.text().endsWith(".pdf") || perilipsi.text()
                            .endsWith(".doc") || perilipsi.text().endsWith(".docx")
                    ) {
                        (activity as KotlinMainActivity).setNameOfPdf(perilipsi.text())
                        (activity as KotlinMainActivity).beginDownload(
                            URL_FOR_PDFs + perilipsiPdf.attr("href"),
                            cookieStringStripped, perilipsi.text()
                        )
                        binding.progressBarPackage.visibility = View.VISIBLE
                    } /*else if (perilipsi.text().equals("Προβολή (H.M.A.)") || perilipsi.text().equals("Προβολή (E.M.A.)")) {


                                    }*/ else {
                        Toast.makeText(
                            context,
                            "Not a valid file to download. Please try another one.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
            } else if (checkElement(
                    doc.select("td[id=form1:grdSPCLink-0-0]").select(".iceCmdLnk").first()
                )
            ) {
                val perilipsi =
                    doc.select("td[id=form1:grdSPCLink-0-0]").select(".iceCmdLnk").first()
                binding.perilipsiXaraktiristikonTextView.text = perilipsi.text()

                /*final Element perilipsiPdf = doc.select("div[id=form1:orDrugSPC_cont]").select("a[href]").first();*/

                binding.perilipsiXaraktiristikonTextView.setOnClickListener(
                    View.OnClickListener {
                        val findString = perilipsi.attr("onclick")
                        val iend = findString.indexOf(";")

                        /*Toast.makeText(context, perilipsi.attr("onclick").substring(13, iend), Toast.LENGTH_LONG).show();*/
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(
                            perilipsi.attr("onclick").substring(13, iend)
                        )
                        if (intent.resolveActivity(context!!.packageManager) != null) {
                            startActivity(intent)
                        }
                    })
            }


            //Filo Odigion
            if (checkElement(
                    doc.select("div[id=form1:orDrugPL_cont]").select(".iceOutLnk").first()
                )
            ) {
                val filoOdigion =
                    doc.select("div[id=form1:orDrugPL_cont]").select(".iceOutLnk").first()
                binding.filoOdigionTextView.text = filoOdigion.text()
                val filoOdigionPdf =
                    doc.select("div[id=form1:orDrugPL_cont]").select("a[href]").first()
                binding.filoOdigionTextView.setOnClickListener(View.OnClickListener { /*Intent myWebLink = new Intent(Intent.ACTION_VIEW);
                                    myWebLink.setData(Uri.parse(URL_FOR_PDFs + filoOdigionPdf.attr("href")));
                                    if (myWebLink.resolveActivity(context.getPackageManager()) != null) {
                                        startActivity(myWebLink);
                                    }*/
                    /*((DetailsActivity) Objects.requireNonNull(getActivity())).viewPdf();*/
                    if (filoOdigion.text().endsWith(".pdf") || filoOdigion.text()
                            .endsWith(".doc") || filoOdigion.text().endsWith(".docx")
                    ) {
                        (activity as KotlinMainActivity).setNameOfPdf(
                            filoOdigion.text()
                        )
                        (activity as KotlinMainActivity).beginDownload(
                            URL_FOR_PDFs + filoOdigionPdf.attr("href"),
                            cookieStringStripped, filoOdigion.text()
                        )
                        binding.progressBarPackage.visibility = View.VISIBLE
                    } /*else if (filoOdigion.text().equals("Προβολή (H.M.A.)") || filoOdigion.text().equals("Προβολή (E.M.A.)")) {


                                    }*/ else {
                        Toast.makeText(
                            context,
                            "Not a valid file to download. Please try another one.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
            } else if (checkElement(
                    doc.select("td[id=form1:grdPLLink-0-0]").select(".iceCmdLnk").first()
                )
            ) {
                val filoOdigion =
                    doc.select("td[id=form1:grdPLLink-0-0]").select(".iceCmdLnk").first()
                binding.filoOdigionTextView.text = filoOdigion.text()
                binding.filoOdigionTextView.setOnClickListener(View.OnClickListener {
                    val findString = filoOdigion.attr("onclick")
                    val iend = findString.indexOf(";")

                    /*Toast.makeText(context, filoOdigion.attr("onclick").substring(13, iend), Toast.LENGTH_LONG).show();*/
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(
                        filoOdigion.attr("onclick").substring(13, iend)
                    )
                    if (intent.resolveActivity(context!!.packageManager) != null) {
                        startActivity(intent)
                    }
                })
            }

            //ekthesi aksiologisis
            if (checkElement(doc.select("td[id=form1:grdPAR-0-1]").select(".iceCmdLnk").first())) {
                val ekthesiAksiologisis =
                    doc.select("td[id=form1:grdPAR-0-1]").select(".iceCmdLnk").first()
                binding.ekthesiAksiologisisTextView.text = ekthesiAksiologisis.text()
                binding.ekthesiAksiologisisTextView.setOnClickListener(View.OnClickListener {
                    if (ekthesiAksiologisis.text()
                            .endsWith(".pdf") || ekthesiAksiologisis.text()
                            .endsWith(".doc") || ekthesiAksiologisis.text().endsWith(".docx")
                    ) {
                        (activity as KotlinMainActivity).setNameOfPdf(
                            ekthesiAksiologisis.text()
                        )
                        (Objects.requireNonNull(activity) as KotlinMainActivity).beginDownload(
                            URL_FOR_PDFs + ekthesiAksiologisis.attr("href"),
                            cookieStringStripped, ekthesiAksiologisis.text()
                        )
                        binding.webViewPackage.visibility = View.VISIBLE
                    } else if (ekthesiAksiologisis.text() == "Προβολή (H.M.A.)" || ekthesiAksiologisis.text() == "Προβολή (E.M.A.)") {
                        val findString = ekthesiAksiologisis.attr("onclick")
                        val iend = findString.indexOf(";")
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(
                            ekthesiAksiologisis.attr("onclick").substring(13, iend)
                        )
                        if (intent.resolveActivity(context!!.packageManager) != null) {
                            startActivity(intent)
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "Not a valid file to download ekthesi. Please try another one.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
            }
        }
    }

    private fun logAll(html: String) {
        val maxLogSize = 1000
        for (i in 0..html.length / maxLogSize) {
            val start = i * maxLogSize
            var end = (i + 1) * maxLogSize
            end = if (end > html.length) html.length else end
            Log.e("YES_EXIST_ALL", html.substring(start, end))
        }
    }

    fun makeProgressBarInVisible() {
        binding.progressBarPackage.visibility = View.GONE
    }

    private fun checkElement(elem: Element?): Boolean {
        return elem != null
    }

    fun backPressButton() {
        binding.webViewPackage.loadUrl("javascript:(function(){l=document.getElementById('form1:btnBack');e=document.createEvent('HTMLEvents');e.initEvent('click',true,true);l.dispatchEvent(e);})()")

        /*if (getView() == null) {
            return;
        }

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    // handle back button's click listener
                    return true;
                }
                return false;
            }
        });*/
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PackageFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PackageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


}
