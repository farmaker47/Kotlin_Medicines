package com.george.kotlin_medicines

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.ahmadrosid.svgloader.SvgLoader
import com.george.kotlin_medicines.databinding.FragmentIngredientBinding
import com.ms.square.android.expandabletextview.ExpandableTextView
import com.squareup.picasso.Picasso
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.FormElement
import java.io.IOException
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val NAME_OF_MEDICINES = "name_of_medicines"

/**
 * A simple [Fragment] subclass.
 * Use the [IngredientFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class IngredientFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var ingredient_name: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentIngredientBinding

    private var ingredientName = ""
    private val DRUGS_CA = "https://www.drugbank.ca/drugs"
    private var builderImage: StringBuilder? = null
    private var builderInfo: StringBuilder? = null
    var arrayForChoiceUrl: ArrayList<String>? = null
    var arrayForChoiceText: ArrayList<String>? = null
    private var parsedText: String? = null
    private var parsedInfo: String? = null
    private var isPresent = true
    private var textDrastiki: TextView? = null
    private var linearDrastiki: LinearLayout? = null
    private var textViewResults: ExpandableTextView? = null
    private var imageMeds: ImageView? = null
    private var linearChoice: LinearLayout? = null
    private val TAG = "DrastikiImage"
    private val drastikiGeneral: String? = null
    private var ingredientProgressBar: ProgressBar? = null
    var isActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            ingredient_name = it.getString(NAME_OF_MEDICINES)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_ingredient,
            container,
            false
        )

        linearDrastiki = binding.linearDrastiki
        textDrastiki = binding.textDrastiki
        textViewResults = binding.expandTextView
        imageMeds = binding.imageMeds
        linearChoice = binding.linearChoice
        ingredientProgressBar = binding.progressIngredient


        pingAndGet(DRUGS_CA)

        Log.v("NAME", "$ingredient_name")
        return binding.root
    }

    //Fetch structure and description
    private fun pingAndGet(url: String) {
        arrayForChoiceText = ArrayList<String>()
        arrayForChoiceUrl = ArrayList()
        builderImage = java.lang.StringBuilder()
        builderInfo = java.lang.StringBuilder()
        textDrastiki!!.text = ""
        textViewResults!!.text = ""
        Thread(Runnable {
            val cookies =
                HashMap<String, String>()
            try {
                if (url == "https://www.drugbank.ca/drugs") {
                    val loginFormResponse =
                        Jsoup.connect(DRUGS_CA)
                            .method(Connection.Method.GET)
                            .userAgent("Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36")
                            .execute()
                    cookies.putAll(loginFormResponse.cookies())
                    //find the form
                    val loginForm = loginFormResponse.parse()
                        .select(".form-inline").first() as FormElement

                    //fill info in element
                    if (loginForm != null) {
                        val loginField =
                            loginForm.select(".search-query").first()
                        loginField?.`val`(ingredientName)

                        //execute
                        val loginActionResponse =
                            loginForm.submit()
                                .data(".search-query", ".search-query")
                                .cookies(loginFormResponse.cookies())
                                .userAgent("Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36")
                                .execute()
                        val arrayForTextView =
                            ArrayList<String>()
                        builderImage!!.append("https://www.drugbank.ca")
                        val doc = loginActionResponse.parse()
                        //check if table exists
                        if (checkElement(
                                doc.select("a[class=moldbi-vector-thumbnail]").first()
                            )
                        ) {
                            val imageUrl =
                                doc.select("a[class=moldbi-vector-thumbnail]")
                            for (element in imageUrl) {
                                parsedText = element.attr("href")
                            }
                            builderImage!!.append(parsedText)

                            //text
                            val tag = doc.getElementsByTag("p")
                            for (element in tag) {
                                val text = element.text()
                                arrayForTextView.add(text)
                            }
                            if (arrayForTextView.size == 0) {
                                builderInfo!!.append("No text to display")
                            } else if (arrayForTextView.size == 1) {
                                builderInfo!!.append(arrayForTextView[0])
                            } else if (arrayForTextView.size == 2) {
                                builderInfo!!.append(arrayForTextView[0]).append("\n\n")
                                    .append(arrayForTextView[1])
                            } else if (arrayForTextView.size == 3) {
                                builderInfo!!.append(arrayForTextView[0]).append("\n\n")
                                    .append(arrayForTextView[1]).append("\n\n")
                                    .append(arrayForTextView[2])
                            } else if (arrayForTextView.size == 4) {
                                builderInfo!!.append(arrayForTextView[0]).append("\n\n")
                                    .append(arrayForTextView[1]).append("\n\n")
                                    .append(arrayForTextView[2]).append("\n\n")
                                    .append(arrayForTextView[3])
                            } else {
                                builderInfo!!.append(arrayForTextView[0]).append("\n\n")
                                    .append(arrayForTextView[1]).append("\n\n")
                                    .append(arrayForTextView[2]).append("\n\n")
                                    .append(arrayForTextView[3])
                            }
                            parsedInfo = builderInfo.toString()
                        } else if (checkElement(
                                doc.select("div[class=unearth-search-hit my-1]")
                                    .select("h2[class=hit-link]").first()
                            )
                        ) {
                            isPresent = false
                            val names =
                                doc.select("div[class=unearth-search-hit my-1]")
                                    .select("h2[class=hit-link]")
                            for (element in names) {
                                val text = element.text()
                                arrayForChoiceText!!.add(text)
                                val aElem =
                                    element.getElementsByTag("a")
                                for (small in aElem) {
                                    val link = small.attr("href")
                                    arrayForChoiceUrl!!.add(link)
                                }
                            }
                        } else if (!checkElement(
                                doc.select("div[class=unearth-search-hit my-1]")
                                    .select("h2[class=hit-link]").first()
                            )
                        ) {
                            isPresent = true
                        }
                        val rows =
                            doc.select("dd[class=col-md-10 col-sm-8]")
                    }
                } else {
                    val loginFormResponseTrial =
                        Jsoup.connect(url)
                            .method(Connection.Method.GET)
                            .userAgent("Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36")
                            .execute()
                    val arrayForTextView =
                        ArrayList<String>()
                    builderImage!!.append("https://www.drugbank.ca")
                    val doc = loginFormResponseTrial.parse()
                    //check if table exists
                    if (checkElement(doc.select("a[class=moldbi-vector-thumbnail]").first())) {
                        val imageUrl =
                            doc.select("a[class=moldbi-vector-thumbnail]")
                        for (element in imageUrl) {
                            parsedText = element.attr("href")
                        }
                        builderImage!!.append(parsedText)
                        //text
                        val tag = doc.getElementsByTag("p")
                        for (element in tag) {
                            val text = element.text()
                            arrayForTextView.add(text)
                        }
                        if (arrayForTextView.size == 0) {
                            builderInfo!!.append("No text to display")
                        } else if (arrayForTextView.size == 1) {
                            builderInfo!!.append(arrayForTextView[0])
                        } else if (arrayForTextView.size == 2) {
                            builderInfo!!.append(arrayForTextView[0]).append("\n\n")
                                .append(arrayForTextView[1])
                        } else if (arrayForTextView.size == 3) {
                            builderInfo!!.append(arrayForTextView[0]).append("\n\n")
                                .append(arrayForTextView[1]).append("\n\n")
                                .append(arrayForTextView[2])
                        } else if (arrayForTextView.size == 4) {
                            builderInfo!!.append(arrayForTextView[0]).append("\n\n")
                                .append(arrayForTextView[1]).append("\n\n")
                                .append(arrayForTextView[2]).append("\n\n")
                                .append(arrayForTextView[3])
                        } else {
                            builderInfo!!.append(arrayForTextView[0]).append("\n\n")
                                .append(arrayForTextView[1]).append("\n\n")
                                .append(arrayForTextView[2]).append("\n\n")
                                .append(arrayForTextView[3])
                        }
                        parsedInfo = builderInfo.toString()
                    } else if (checkElement(
                            doc.select("div[class=unearth-search-hit my-1]")
                                .select("h2[class=hit-link]").first()
                        )
                    ) {
                        isPresent = false
                        val names =
                            doc.select("div[class=unearth-search-hit my-1]")
                                .select("h2[class=hit-link]")
                        for (element in names) {
                            val text = element.text()
                            arrayForChoiceText!!.add(text)
                            val aElem = element.getElementsByTag("a")
                            for (small in aElem) {
                                val link = small.attr("href")
                                arrayForChoiceUrl!!.add(link)
                            }
                        }
                    } else if (!checkElement(
                            doc.select("div[class=unearth-search-hit my-1]")
                                .select("h2[class=hit-link]").first()
                        )
                    ) {
                        isPresent = true
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            activity!!.runOnUiThread(Runnable {
                if (builderImage.toString() != "https://www.drugbank.ca") {
                    ingredientProgressBar!!.visibility = View.INVISIBLE
                    SvgLoader.pluck()
                        .with(activity)
                        .setPlaceHolder(R.drawable.recipe_icon, R.drawable.recipe_icon)
                        .load(builderImage.toString(), imageMeds)
                    textViewResults!!.text = parsedInfo
                    textDrastiki!!.text = ingredientName
                    linearDrastiki!!.visibility = View.VISIBLE
                } else if (builderImage.toString() == "https://www.drugbank.ca" && isPresent) {
                    ingredientProgressBar!!.visibility = View.INVISIBLE
                    Picasso.get().load(R.drawable.recipe_icon).into(imageMeds)
                    textViewResults!!.text = getString(R.string.drastikiNoResults)
                    Log.i("LATHOS1", builderImage.toString())
                    linearDrastiki!!.visibility = View.VISIBLE
                } else if (builderImage.toString() == "https://www.drugbank.ca" && !isPresent) {
                    ingredientProgressBar!!.visibility = View.INVISIBLE
                    Picasso.get().load(R.drawable.recipe_icon).into(imageMeds)
                    textDrastiki!!.text = ingredientName
                    Log.i("LATHOS2", builderImage.toString())
                    textViewResults!!.text = getString(R.string.noresultTryBelow)
                    for (i in arrayForChoiceUrl!!.indices) {
                        val name: String = arrayForChoiceText!!.get(i)
                        val urlText = arrayForChoiceUrl!![i]

                        //Creating a view
                        val ingredient = TextView(activity)
                        ingredient.text = name
                        val params = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        params.setMargins(8, 8, 8, 8)
                        ingredient.layoutParams = params
                        ingredient.textSize = 18f
                        ingredient.textAlignment = View.TEXT_ALIGNMENT_CENTER
                        ingredient.paintFlags = ingredient.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                        ingredient.setTextColor(Color.BLUE)
                        ingredient
                            .setOnClickListener { /*Toast.makeText(IngredientActivity.this,  urlText, Toast.LENGTH_LONG).show();*/
                                ingredientName = arrayForChoiceText!!.get(i)
                                ingredientProgressBar!!.visibility = View.VISIBLE
                                imageMeds!!.setImageDrawable(null)
                                pingAndGet(DRUGS_CA)
                                linearChoice!!.removeAllViews()
                            }
                        linearChoice!!.addView(ingredient)
                    }
                    linearDrastiki!!.visibility = View.VISIBLE
                }
            })
        }).start()
    }

    private fun checkElement(elem: Element?): Boolean {
        return elem != null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment IngredientFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            IngredientFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
