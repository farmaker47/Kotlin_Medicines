package com.george.kotlin_medicines

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.george.kotlin_medicines.databinding.FragmentSearchFragmentNavigationBinding
import com.george.kotlin_medicines.utils.SoloupisEmptyRecyclerView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SearchFragmentNavigation.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragmentNavigation : Fragment() , SearchFragmentNavigationAdapter.SearchClickItemListener{
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var webView: WebView
    private lateinit var binding: FragmentSearchFragmentNavigationBinding
    val URL_TO_SERVE = "https://services.eof.gr/drugsearch/SearchName.iface"

    private lateinit var editTextView: EditText
    private lateinit var mRecyclerViewSearchFragment: SoloupisEmptyRecyclerView
    private lateinit var imageViewSearchFragment: ImageView
    private lateinit var progressBarSearchFragment: ProgressBar
    private var hitaList: ArrayList<String> = arrayListOf("George")
    private lateinit var mSearchFragmentNavigationAdapter: SearchFragmentNavigationAdapter

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
        webView = binding.webViewEof
        editTextView = binding.autoSearchNavigation
        mRecyclerViewSearchFragment = binding.recyclerViewSearchFragment
        imageViewSearchFragment = binding.imageSearchFragment
        progressBarSearchFragment = binding.progressSearchFragment

        //setting the empty view, only with custom Recycler view
        //setting the empty view, only with custom Recycler view
        mRecyclerViewSearchFragment.setEmptyView(imageViewSearchFragment)

        mRecyclerViewSearchFragment.setHasFixedSize(true)
        mRecyclerViewSearchFragment.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        mSearchFragmentNavigationAdapter =
            SearchFragmentNavigationAdapter(activity!!, hitaList, this)
        mRecyclerViewSearchFragment.adapter = mSearchFragmentNavigationAdapter


        //Enable Javascript
        webView.settings.javaScriptEnabled = true
        //Clear All and load url
        webView.loadUrl(URL_TO_SERVE)



        return binding.root
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

    override fun onListItemClick(itemIndex: Int, sharedImage: ImageView?, type: String?) {
        TODO("Not yet implemented")
    }
}
