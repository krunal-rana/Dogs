package com.lionheartapps.rk.dogs.view

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.telephony.SmsManager
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.lionheartapps.rk.dogs.R
import com.lionheartapps.rk.dogs.databinding.FragmentDetailBinding
import com.lionheartapps.rk.dogs.databinding.SendSmsDialogBinding
import com.lionheartapps.rk.dogs.model.DogBreed
import com.lionheartapps.rk.dogs.model.DogPalette
import com.lionheartapps.rk.dogs.model.SmsInfo
import com.lionheartapps.rk.dogs.viewmodel.DetailsViewModel

/**
 * A simple [Fragment] subclass.
 */
class DetailFragment : Fragment() {

    private var dogUuidFirst = 0

    private lateinit var viewModel: DetailsViewModel


    private lateinit var dataBinding: FragmentDetailBinding

    private var sendSmsStarted = false
    private var currentDog: DogBreed? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        // Inflate the layout for this fragment
        dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail, container, false)
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
            currentDog = dog

            dog?.let {
                dataBinding.dog = dog
                it.imageUrl?.let {
                    setupBackgroundColor(it)
                }
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

    private fun setupBackgroundColor(url: String) {
        Glide.with(this)
            .asBitmap()
            .load(url)
            .into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {

                }

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    Palette.from(resource)
                        .generate { palette ->
                            val intColor = palette?.vibrantSwatch?.rgb ?: 0
                            val myPalette = DogPalette(intColor)
                            dataBinding.palette = myPalette
                        }
                }

            })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.details_menu, menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_send_sms -> {
                sendSmsStarted = true
                (activity as MainActivity).checkSmsPermission()
            }

            R.id.action_share -> {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out this dog breed")
                shareIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    "${currentDog?.dogBreed} bred for ${currentDog?.breedFor} \n\nImage => ${currentDog?.imageUrl} \n\n"
                )
                shareIntent.putExtra(Intent.EXTRA_STREAM, currentDog?.imageUrl)
                context?.startActivity(Intent.createChooser(shareIntent, "Share With"))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun onPermissionResult(permissionGranted: Boolean) {
        if (sendSmsStarted && permissionGranted) {
            context?.let {
                val smsInfo = SmsInfo(
                    "",
                    "${currentDog?.dogBreed} bred for ${currentDog?.breedFor}",
                    currentDog?.imageUrl
                )

                val dialogBinding = DataBindingUtil.inflate<SendSmsDialogBinding>(
                    LayoutInflater.from(it),
                    R.layout.send_sms_dialog,
                    null,
                    false
                )
                AlertDialog.Builder(it)
                    .setView(dialogBinding.root)
                    .setPositiveButton("Send Sms") { dialog, which ->
                        if (!dialogBinding.smsDestination.text.isNullOrBlank()) {
                            smsInfo.to = dialogBinding.smsDestination.text.toString()
                            sendSms(smsInfo)
                        }
                    }
                    .setNegativeButton("Cancel") { dialog, which ->

                    }
                    .show()

                dialogBinding.smsInfo = smsInfo // getting data
            }


        }
    }

    fun sendSms(smsInfo: SmsInfo) {
        val intent = Intent(context, MainActivity::class.java)
        val pi = PendingIntent.getActivity(context, 0, intent, 0)
        val sms = SmsManager.getDefault()
        sms.sendTextMessage(smsInfo.to, null, smsInfo.text, pi, null)
    }
}
