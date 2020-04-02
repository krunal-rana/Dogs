package com.lionheartapps.rk.dogs.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.lionheartapps.rk.dogs.R
import com.lionheartapps.rk.dogs.databinding.FragmentDetailBinding
import com.lionheartapps.rk.dogs.util.getProgressDrawable
import com.lionheartapps.rk.dogs.util.loadImage
import com.lionheartapps.rk.dogs.viewmodel.DetailsViewModel
import kotlinx.android.synthetic.main.fragment_detail.*

/**
 * A simple [Fragment] subclass.
 */
class DetailFragment : Fragment() {

    private var dogUuidFirst = 0

    private lateinit var viewModel: DetailsViewModel


    private lateinit var dataBinding: FragmentDetailBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        dataBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_detail, container, false)
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            dogUuidFirst = DetailFragmentArgs.fromBundle(it).dogUuid
        }

        viewModel = ViewModelProviders.of(this).get(DetailsViewModel::class.java)
        viewModel.fetch(dogUuidFirst)

        observViewModel()
    }

    @SuppressLint("FragmentLiveDataObserve")
    fun observViewModel() {
        viewModel.dogLiveData.observe(this, Observer { dog ->
            dog?.let {
                dataBinding.dog = dog
//                tvDogName.text = dog.dogBreed.toString()
//                tvDogPurpose.text = dog.breedFor.toString()
//                tvDogTemper.text = dog.temperament.toString()
//                tvDogLifeSpan.text = dog.lifeSpan.toString()
//                context?.let {
//                    imageDog.loadImage(dog.imageUrl, getProgressDrawable(it))
//                }

            }

        })
    }



}
