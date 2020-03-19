package com.george.kotlin_medicines

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.george.kotlin_medicines.databinding.ActivityScrollingDetailsFragmentBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val NAME_OF_MEDICINES = "name_of_medicines"

/**
 * A simple [Fragment] subclass.
 * Use the [PackageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PackageFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var medicine_name: String? = null
    private var param2: String? = null
    private lateinit var binding: ActivityScrollingDetailsFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            medicine_name = it.getString(NAME_OF_MEDICINES)
            param2 = it.getString(ARG_PARAM2)
        }
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

        binding.titleTextViewGray.text = medicine_name

        return binding.root
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
