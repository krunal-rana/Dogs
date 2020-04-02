package com.lionheartapps.rk.dogs.view

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager

import com.lionheartapps.rk.dogs.R
import com.lionheartapps.rk.dogs.viewmodel.ListViewModel
import kotlinx.android.synthetic.main.fragment_list.*
import java.time.LocalDate.of
import java.util.stream.Collector.of

/**
 * A simple [Fragment] subclass.
 */
class ListFragment : Fragment() {

    private lateinit var viewModel: ListViewModel
    private val dogListAdapter = DogsListAdapter(arrayListOf())


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false)

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(ListViewModel::class.java)
        viewModel.refresh()


        recyclerView_dogs.apply{
            layoutManager = LinearLayoutManager(context)
            adapter = dogListAdapter
        }
        refreshLayout.setOnRefreshListener {
            recyclerView_dogs.visibility = View.GONE
            tvListError.visibility = View.GONE
            progressBar.visibility = View.VISIBLE

            viewModel.refreshBypassCache() // bypasss cache
            refreshLayout.isRefreshing = false
        }
        observeViewModel()
    }

    @SuppressLint("FragmentLiveDataObserve")
    fun observeViewModel(){

        viewModel.dogs.observe(this, Observer {dogs ->
            dogs?.let {
                recyclerView_dogs.visibility = View.VISIBLE
                dogListAdapter.updateDogList(dogs)
            }

        })
        viewModel.dogsLoadError.observe(this, Observer { isError ->
            isError?.let {
                tvListError.visibility = if(it) View.VISIBLE else View.GONE
            }
        })

        viewModel.loading.observe(this, Observer { isLoading ->
            isLoading?.let {
                progressBar.visibility = if(it) View.VISIBLE else View.GONE
                if(it){
                    recyclerView_dogs.visibility = View.GONE
                    tvListError.visibility = View.GONE
                }
            }
        })


    }
}
