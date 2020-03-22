package com.george.kotlin_medicines

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.JsonReader
import android.util.JsonToken
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.webkit.ValueCallback
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.george.kotlin_medicines.databinding.FragmentSearchFragmentNavigationBinding
import com.george.view_models.SearchFragmentNavigationViewModel
import kotlinx.android.synthetic.main.activity_scrolling_details_fragment.view.*
import kotlinx.android.synthetic.main.fragment_search_fragment_navigation.view.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.IOException
import java.io.StringReader
import java.util.*
import kotlin.collections.ArrayList

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val NAME_OF_MEDICINES = "name_of_medicines"
private const val URL_TO_SERVE = "https://services.eof.gr/drugsearch/SearchName.iface"

/**
 * A simple [Fragment] subclass.
 * Use the [SearchFragmentNavigation.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragmentNavigation : Fragment(),
    SearchFragmentNavigationAdapter.SearchClickItemListener {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentSearchFragmentNavigationBinding

    /*
        private var hitaList: ArrayList<String> = ArrayList()
    */
    private lateinit var mSearchFragmentNavigationAdapter: SearchFragmentNavigationAdapter
    private lateinit var timer: Timer

    private lateinit var viewModel: SearchFragmentNavigationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_search_fragment_navigation,
            container,
            false
        )

        //set title
        activity?.title = getString(R.string.titleSearch)

        //Get the viewmodel
        viewModel = ViewModelProvider(this).get(SearchFragmentNavigationViewModel::class.java)
        //viewModel.setStringOfEditText(binding.autoSearchNavigation.text.toString().trim())

        //Upon creation we check if there is internet connection
        val connMgr =
            activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected) {
            //TODO
        } else {
            Toast.makeText(activity, R.string.please_connect_to_internet, Toast.LENGTH_SHORT).show()
        }

        //Initialize timer
        timer = Timer()

        //setting the empty view, only with custom Recycler view
        binding.recyclerViewSearchFragment.setEmptyView(binding.imageSearchFragment)
        binding.recyclerViewSearchFragment.setHasFixedSize(true)
        binding.recyclerViewSearchFragment.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        mSearchFragmentNavigationAdapter =
            SearchFragmentNavigationAdapter(activity!!, viewModel.currentList, this)
        binding.recyclerViewSearchFragment.adapter = mSearchFragmentNavigationAdapter


        //EditText with timer
        binding.autoSearchNavigation.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
                // user is typing: reset already started timer (if existing)
                timer.cancel()
            }

            override fun afterTextChanged(editable: Editable) {
                if (binding.autoSearchNavigation.length() >= 4 && networkInfo != null && networkInfo.isConnected) {

                    timer = Timer()
                    timer.schedule(object : TimerTask() {
                        override fun run() {

                            //we determine if it is on creation or after rotation
                            if (savedInstanceState == null) {
                                activity!!.runOnUiThread {
                                    hideKeyboard()
                                    fetchInfo(binding.autoSearchNavigation.text.toString().trim())
                                    binding.progressSearchFragment.visibility = View.VISIBLE
                                    //set string of edittext in viewmodel
                                    viewModel.setStringOfEditText(
                                        binding.autoSearchNavigation.text.toString().trim()
                                    )
                                }
                            } else if (savedInstanceState != null && binding.autoSearchNavigation.text.toString()
                                    .trim() == viewModel.stringOfEditText
                            ) {
                                activity!!.runOnUiThread {
                                    Log.i("SAME_EDIT_EQUALS", viewModel.stringOfEditText)
                                }
                            } else if (savedInstanceState != null && binding.autoSearchNavigation.text.toString()
                                    .trim() != viewModel.stringOfEditText
                            ) {
                                Log.i("SAME_EDIT_NOT_EQUALS", viewModel.stringOfEditText)
                                activity!!.runOnUiThread {
                                    hideKeyboard()
                                    fetchInfo(binding.autoSearchNavigation.text.toString().trim())
                                    binding.progressSearchFragment.visibility = View.VISIBLE
                                    //set string of edittext in viewmodel
                                    viewModel.setStringOfEditText(
                                        binding.autoSearchNavigation.text.toString().trim()
                                    )
                                }
                            }
                        }
                    }, 1000)
                }
            }
        })

        //clicking X button at right inside autocomplete
        binding.autoSearchNavigation.setOnTouchListener(OnTouchListener { view, motionEvent ->
            val DRAWABLE_LEFT = 0
            val DRAWABLE_TOP = 1
            val DRAWABLE_RIGHT = 2
            val DRAWABLE_BOTTOM = 3
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                if (motionEvent.rawX >= binding.autoSearchNavigation.right - binding.autoSearchNavigation.compoundDrawables[DRAWABLE_RIGHT].bounds.width()
                ) {
                    // your action here
                    //First click back button
                    binding.webViewEof.loadUrl("javascript:(function(){l=document.getElementById('form1:btnBack');e=document.createEvent('HTMLEvents');e.initEvent('click',true,true);l.dispatchEvent(e);})()")
                    //then do process
                    binding.autoSearchNavigation.setText("")
                    clearDataOfList()
                    showKeyboard()
                    return@OnTouchListener true
                } else if (motionEvent.rawX < binding.autoSearchNavigation.compoundDrawables[DRAWABLE_LEFT].bounds.width() - binding.autoSearchNavigation.left) {

                    //TODO
                    /*IntentIntegrator integrator = new IntentIntegrator(getActivity());
                            integrator.initiateScan();*/
                    return@OnTouchListener true
                }
            }
            false
        })

        //Enable Javascript
        binding.webViewEof.settings.javaScriptEnabled = true
        //Clear All and load url
        binding.webViewEof.loadUrl(URL_TO_SERVE)



        return binding.root
    }

    override fun onResume() {
        super.onResume()
        //make edittext with no letters inside to avoid loading again
        binding.autoSearchNavigation.setText("")

        binding.webViewEof.setWebViewClient(object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                val handler = Handler()
                handler.postDelayed(
                    { binding.webViewEof.loadUrl("javascript:(function(){l=document.getElementById('form1:btnBack');e=document.createEvent('HTMLEvents');e.initEvent('click',true,true);l.dispatchEvent(e);})()") },
                    500
                )
            }
        })

        binding.webViewEof.loadUrl(URL_TO_SERVE)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SearchFragmentNavigation.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchFragmentNavigation().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


    private fun showKeyboard() {
        val show = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        show.showSoftInput(binding.autoSearchNavigation, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val hide = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        hide.hideSoftInputFromWindow(binding.autoSearchNavigation.getApplicationWindowToken(), 0)
    }

    private fun clearDataOfList() {
        mSearchFragmentNavigationAdapter.setHitsData(ArrayList())
        Objects.requireNonNull(binding.recyclerViewSearchFragment.adapter)?.notifyDataSetChanged()
    }

    override fun onListItemClick(itemIndex: Int, sharedImage: ImageView?, type: String?) {
        Log.e("Clicked", type)
        //click specific item position
        binding.webViewEof.loadUrl("javascript:(function(){l=document.getElementById('form1:tblResults:$itemIndex:lnkDRNAME');e=document.createEvent('HTMLEvents');e.initEvent('click',true,true);l.dispatchEvent(e);})()")

        //navigate to second fragment
        //findNavController().navigate(R.id.action_searchFragmentNavigation_to_packageFragment)

        //making animation above api


        //making animation above api
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // bundle for the transition effect
            Log.v("transition", sharedImage!!.transitionName)
            // bundle for the transition effect
            val bundle = Bundle()
            bundle.putString(NAME_OF_MEDICINES, type)
            /*val bundle: Bundle = ActivityOptionsCompat
                .makeSceneTransitionAnimation(
                    ,
                    sharedImage,
                    sharedImage!!.transitionName
                ).toBundle()*/
            val extras = FragmentNavigatorExtras(
                sharedImage to "transition_photo"
            )

            val action = SearchFragmentNavigationDirections.actionSearchFragmentNavigationToPackageFragment(uri = "transition_photo")
            /*findNavController().navigate(
                R.id.action_searchFragmentNavigation_to_packageFragment,
                bundle
            )*/
            findNavController().navigate(
                action,
                extras
            )
        } else {

            val bundleLollipop = Bundle()
            bundleLollipop.putString(NAME_OF_MEDICINES, type)
            findNavController().navigate(
                R.id.action_searchFragmentNavigation_to_packageFragment,
                bundleLollipop
            )

        }

    }

    private fun fetchInfo(queryString: String) {
        //Put value
        binding.webViewEof.loadUrl("javascript:(function(){l=document.getElementById('form1:txtDrname').value='$queryString';})()")
        val connMgr =
            context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        // If there is a network connection, fetch info
        if (networkInfo != null && networkInfo.isConnected) {
            //Click button
            binding.webViewEof.loadUrl("javascript:(function(){l=document.getElementById('form1:btnSubmit');e=document.createEvent('HTMLEvents');e.initEvent('click',true,true);l.dispatchEvent(e);})()")

            //Wait 2 seconds for webview to load
            val handler = Handler()
            handler.postDelayed({
                binding.webViewEof.evaluateJavascript(
                    "(function() { return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); })();",
                    ValueCallback<String?> { html ->
                        //Make progressBar disappear
                        binding.progressSearchFragment.visibility = View.INVISIBLE
                        val reader = JsonReader(StringReader(html))
                        reader.isLenient = true
                        try {
                            if (reader.peek() == JsonToken.STRING) {
                                val domStr = reader.nextString()
                                domStr?.let { parseSecondColumn(it) }
                            }
                        } catch (e: IOException) {
                            // handle exception
                        }
                    })
            }, 2000)
        } else {
            Toast.makeText(context, R.string.no_internet, Toast.LENGTH_LONG).show()
        }
    }

    @Throws(IOException::class)
    private fun parseSecondColumn(html: String) {
        val builder = StringBuilder()
        val arrayForTextView = ArrayList<String>()
        val doc = Jsoup.parse(html)
        if (checkElement(
                doc.select("table[id=form1:tblResults]").first()
            )
        ) {
            //Select column that attribute ends in lnkDRNAME
            val row =
                doc.select("table[id=form1:tblResults]").select(".iceDatTblCol2")
                    .select("a[id$=lnkDRNAME]")
            if (row != null) {
                for (element in row) {
                    val text = element.text()
                    arrayForTextView.add(text)
                }
                if (arrayForTextView.size == 0) {
                    Toast.makeText(activity, R.string.no_results, Toast.LENGTH_LONG).show()
                }
                for (i in arrayForTextView.indices) {
                    builder.append(arrayForTextView[i]).append("\n")
                    var parsedText: String = builder.toString()
                }

                //Set value to viewMOdel
                viewModel.setList(arrayForTextView)
                //Show results
                mSearchFragmentNavigationAdapter.setHitsData(viewModel.currentList)
                Objects.requireNonNull(binding.recyclerViewSearchFragment.adapter)
                    ?.notifyDataSetChanged()
                //we reset position to 0
                /*binding.recyclerViewSearchFragment.smoothScrollToPosition(0)
                binding.recyclerViewSearchFragment.layoutManager?.scrollToPosition(0)*/

                //running the animation at the beggining of showing the list
                runLayoutAnimation(binding.recyclerViewSearchFragment)
            }
        } else {
            //In case server is down for maintenance
            Toast.makeText(activity, R.string.eof_error, Toast.LENGTH_LONG).show()
            Objects.requireNonNull(activity)!!.finish()
        }
    }

    fun checkElement(elem: Element?): Boolean {
        return elem != null
    }

    private fun runLayoutAnimation(recyclerView: RecyclerView) {
        val context = recyclerView.context
        val controller =
            AnimationUtils.loadLayoutAnimation(
                context,
                R.anim.layout_animation_fall_down
            )
        recyclerView.layoutAnimation = controller
/*
        Objects.requireNonNull(recyclerView.adapter)?.notifyDataSetChanged()
*/
        recyclerView.scheduleLayoutAnimation()
    }

}
