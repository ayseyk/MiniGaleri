package com.example.minigaleri

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.view.Display
import android.view.Gravity
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.hardware.display.DisplayManagerCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.example.minigaleri.databinding.ActivityMainBinding
import com.example.minigaleri.databinding.PopupSecimBinding
import java.io.File

class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding
    lateinit var resimler : ArrayList<Uri?>
    lateinit var resimUri : Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnResimEkke.setOnClickListener {
            popUpGoster()
        }
        binding.closeButton.isVisible = false
        resimler = arrayListOf(null)
        rcvCreate()

    }
    /*
    Açılış ekranında üstte bir resim ekle butonu olacak. Bu butona basıldığında alertte kamera ve galeri
    seçenekleri olan popup çıkacak. Kamera seçildiğinde Kamera, Galeri seçildiğinde galeri açılacak.
    Kameradan resim çekildiğinde ya da galeriden seçildiğinde butonun altındaki listeye eklenecek.

    Çekilen resimlerin tümü 2 sütun olarak recyclerview ile listelenecek.

    Resime basıldığında o resim aynı ekranda büyük bir şekilde gösterilecek.

    Resime uzun basıldığında ise alert ile sil/vazgeç seçenekleri çıkacak. Sil seçildiğinde o resim
    listeden silinecek ve liste yenilenecek

     */
    fun popUpGoster(){
        var width : Int
        var height : Int

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) //SDK 30 dan büyük eşitse
        {
            val defaultDisplay = DisplayManagerCompat.getInstance(this).getDisplay(Display.DEFAULT_DISPLAY)
            val displayContext = createDisplayContext(defaultDisplay!!)

            width = displayContext.resources.displayMetrics.widthPixels
            height = displayContext.resources.displayMetrics.heightPixels
        }
        else
        {
            height = windowManager.defaultDisplay.height
            width = windowManager.defaultDisplay.width
        }

        val popupBinding = PopupSecimBinding.inflate(layoutInflater)
        val popUyari = PopupWindow(popupBinding.root, width, height)

        popUyari.showAtLocation(popupBinding.root, Gravity.CENTER, 0, 0)

        popupBinding.btnGaleri.setOnClickListener{
            galeriIzinKontrol()
            popUyari.dismiss()
        }
        popupBinding.btnKamera.setOnClickListener{
            kameraIzinKontrol()
            popUyari.dismiss()
        }
        popupBinding.btnIptal.setOnClickListener{
            popUyari.dismiss()
        }
    }
    fun rcvCreate()
    {
        binding.rcvResimler.layoutManager = GridLayoutManager(this, 2, GridLayoutManager
            .VERTICAL, false)
        binding.rcvResimler.adapter = ResimAdapter(this, resimler, ::resimItemClick, ::resimUzunClick)
    }
    fun resimUzunClick(position: Int){
        val adb : AlertDialog.Builder = AlertDialog.Builder(this)
        adb.setTitle("Listeden kaldır").setMessage("Seçilen resmi listeden kaldırmak istediğinize" +
                " emin misiniz?")
        adb.setPositiveButton("sil",DialogInterface.OnClickListener { dialogInterface, i ->
            resimler.removeAt(position)
            binding.rcvResimler.adapter!!.notifyDataSetChanged()
        }).setNegativeButton("vazgeç",null).show()
    }
    fun resimItemClick(position: Int){
        var resim = resimler.get(position)
        binding.imageView.setImageURI(resim)
        binding.closeButton.isVisible = true
        binding.imageView.isVisible = true
        binding.btnResimEkke.isVisible = false
        binding.rcvResimler.isVisible= false
        resmiKapat()
    }
    fun resmiKapat(){
        binding.closeButton.setOnClickListener {
            binding.imageView.isVisible = false
            binding.closeButton.isVisible = false
            binding.btnResimEkke.isVisible = true
            binding.rcvResimler.isVisible= true
        }
    }

    fun galeriIzinKontrol(){
        val requestList = ArrayList<String>()
       var izinDurum = ContextCompat.checkSelfPermission(this, Manifest.permission
        .READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

        if (!izinDurum)
        {
            requestList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (requestList.size == 0)
        {
            galeriAc()
        }
        else
        {
            requestPermissions(requestList.toTypedArray(), 1)
        }
    }
    fun kameraIzinKontrol()
    {
        val requestList = ArrayList<String>()

        var izinDurum = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

        if (!izinDurum)
        {
            requestList.add(Manifest.permission.CAMERA)
        }

        izinDurum = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

        if (!izinDurum)
        {
            requestList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        izinDurum = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

        if (!izinDurum)
        {
            requestList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (requestList.size == 0)
        {
            kameraAc()
        }
        else
        {
            requestPermissions(requestList.toTypedArray(), 0)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        var tumuOnaylandi = true

        for (gr in grantResults)
        {
            if (gr != PackageManager.PERMISSION_GRANTED)
            {
                tumuOnaylandi = false
                break
            }
        }

        if (!tumuOnaylandi)
        {
            var tekrarGosterme = false

            for (permission in permissions)
            {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
                {
                    // reddedildi
                }
                else if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED)
                {
                    // onaylandı
                }
                else
                {
                    // tekrar gösterme seçildi
                    tekrarGosterme = true
                    break
                }
            }

            if (tekrarGosterme)
            {
                val adb = AlertDialog.Builder(this)
                adb.setTitle("İzin Gerekli")
                    .setMessage("Ayarlara giderek tüm izinleri onaylayınız")
                    .setPositiveButton("Ayarlar", {dialog, which ->
                        ayarlarAc()
                    })
                    .setNegativeButton("Vazgeç", null)
                    .show()
            }
        }
        else
        {
            when (requestCode)
            {
                0 -> kameraAc()
                1 -> galeriAc()

            }
        }
    }
    private fun ayarlarAc() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }
    fun galeriAc(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*" //uzantısına bakmadan getirir tüm resim dosyalarını
        galeriRl.launch(intent)
    }
    var galeriRl = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    {
        if(it.resultCode == RESULT_OK){
            val u : Uri = it.data!!.data!! // uriyi getirir
            resimler.add(u)
            binding.rcvResimler.adapter!!.notifyDataSetChanged()
        }
    }
    fun kameraAc()
    {
        resimDosyasiOlustur()
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, resimUri)
        cameraRl.launch(intent)
    }

    var cameraRl= registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    {
        if (it.resultCode == RESULT_OK)
        {
            resimler.add(resimUri)
            binding.rcvResimler.adapter!!.notifyDataSetChanged()
        }
    }


    fun resimDosyasiOlustur()
    {
        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        File.createTempFile("resim", ".jpg", dir).apply {
            resimUri = FileProvider.getUriForFile(this@MainActivity, packageName, this)
        }
    }
}