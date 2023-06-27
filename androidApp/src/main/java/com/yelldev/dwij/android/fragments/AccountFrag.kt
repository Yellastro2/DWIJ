package com.yelldev.dwij.android.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.yelldev.dwij.android.KeyStore
import com.yelldev.dwij.android.KeyStore.Companion.DWIJ_ACC_TOKEN
import com.yelldev.dwij.android.MainActivity
import com.yelldev.dwij.android.R
import com.yelldev.dwij.android.entitis.YaM.yUser

class AccountFrag: Fragment(R.layout.fr_account) {

	val RC_SIGN_IN = 235433
	val TAG = "account frag"

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		val sharedPref = requireActivity().getSharedPreferences(KeyStore.s_preff, AppCompatActivity.MODE_PRIVATE)
		var fName = sharedPref.getString(DWIJ_ACC_TOKEN,"")
		val fYaLogin = sharedPref.getString(com.yelldev.dwij.android.KeyStore.k_ya_login, "")

		if (fName.isNullOrEmpty())
			fName = fYaLogin
		view.findViewById<View>(R.id.fr_acc_logout).setOnClickListener {
			sharedPref.edit().putString(DWIJ_ACC_TOKEN,"").apply()
			(activity as MainActivity).mNavController.navigate(R.id.homeFrag)
		}
		view.findViewById<TextView>(R.id.fr_acc_name).text = fName

		view.findViewById<RecyclerView>(R.id.fr_acc_userlist).adapter =
			UserListAdapter(arrayListOf(
				yUser("","Zefirka","Top 1 princess in univerce",""),
				yUser("","Oleg","profesional gamer",""),
				yUser("","DJ PUZYIRYOK","THIW IS RADIO SHIPUCHKA","")
			))

	}

	class UserListAdapter(val mUsers: ArrayList<yUser>) :
		RecyclerView.Adapter<UserListAdapter.ViewHolder>() {


		class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
			val vLogin: TextView
			val vDesk: TextView
			val vAva: ImageView
			val vOnair: TextView

			init {
				vLogin = view.findViewById(R.id.it_user_name)
				vDesk = view.findViewById(R.id.it_user_deck)
				vAva = view.findViewById(R.id.it_user_ava)
				vOnair = view.findViewById(R.id.it_user_onair)
			}

		}

		override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
			val view = LayoutInflater.from(viewGroup.context)
				.inflate(R.layout.it_user, viewGroup, false)
//			view.layoutParams = ViewGroup.LayoutParams(mFrag.mGridSize,mFrag.mGridSize)

			return ViewHolder(view)
		}

		@SuppressLint("CheckResult")
		override fun onBindViewHolder(vh: ViewHolder, position: Int) {
			val qUser = mUsers[position]
			vh.vOnair.visibility = if (qUser.getOnAir() ) View.VISIBLE else View.GONE
			vh.vLogin.text = qUser.mLogin
			vh.vDesk.text = qUser.mDesk

		}

		override fun getItemCount() = mUsers.size

	}


}