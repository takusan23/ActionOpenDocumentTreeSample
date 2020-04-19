package io.github.takusan23.actionopendocumenttreesample

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val REQUEST_CODE = 816
    lateinit var prefSetting: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefSetting = PreferenceManager.getDefaultSharedPreferences(this)

        path_button.setOnClickListener {
            // ユーザーにアプリで使っていいフォルダを選んでもらう。
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            startActivityForResult(intent, REQUEST_CODE)
        }

        // 保存ボタン
        save_button.setOnClickListener {
            saveFile()
        }

        // 読み込みボタン
        read_button.setOnClickListener {
            readFile()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // リクエストコードが一致&成功のとき
            val uri = data?.data ?: return
            // Uriは再起すると使えなくなるので対策
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            // Uri保存。これでアプリ再起動後も使えます。
            prefSetting.edit {
                putString("uri", uri.toString())
            }
        }
    }

    // ファイル保存
    private fun saveFile() {
        val uri = prefSetting.getString("uri", "")?.toUri() ?: return
        // ファイル操作
        DocumentFile.fromTreeUri(this, uri)?.apply {
            // テキストファイル
            val textFile = if (findFile("test.txt")?.exists() == true) {
                // すでに作成済み
                findFile("test.txt") ?: return@apply
            } else {
                // まだないので新規作成
                createFile("text/plain", "test.txt") ?: return@apply
            }
            // 書き込む
            contentResolver.openOutputStream(textFile.uri)?.apply {
                // Activityに置いたTextEditのテキスト取得
                write(editText.text?.toString()?.toByteArray())
                close()
            }
        }
    }

    // ファイル読み込み
    private fun readFile() {
        val uri = prefSetting.getString("uri", "")?.toUri() ?: return
        // ファイル操作
        DocumentFile.fromTreeUri(this, uri)?.apply {
            // テキストファイル取り出し
            if (findFile("test.txt")?.exists() == false) {
                // なければ終了
                return@apply
            }
            val textFile = findFile("test.txt") ?: return@apply
            // テキスト取り出す
            val text = contentResolver.openInputStream(textFile.uri)?.bufferedReader()?.readLine()
            println(text)
            editText.setText(text)
        }
    }

}
