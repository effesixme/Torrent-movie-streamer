package com.example.torrentstreamer

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var playerView: PlayerView
    private var exoPlayer: ExoPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // UI Components
        val urlInput = findViewById<EditText>(R.id.urlInput)
        val usernameInput = findViewById<EditText>(R.id.usernameInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val rememberCheckbox = findViewById<CheckBox>(R.id.rememberCheckbox)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val searchInput = findViewById<EditText>(R.id.searchInput)
        val searchButton = findViewById<Button>(R.id.searchButton)
        val resultsListView = findViewById<ListView>(R.id.resultsListView)
        val statusText = findViewById<TextView>(R.id.statusText)
        playerView = findViewById(R.id.playerView)
        
        val sharedPref = getSharedPreferences("torrent_prefs", MODE_PRIVATE)
        
        // Carica credenziali salvate
        if (sharedPref.getBoolean("remember", false)) {
            urlInput.setText(sharedPref.getString("url", ""))
            usernameInput.setText(sharedPref.getString("username", ""))
            passwordInput.setText(sharedPref.getString("password", ""))
            rememberCheckbox.isChecked = true
        }
        
        // Login
        loginButton.setOnClickListener {
            val url = urlInput.text.toString()
            if (url.isEmpty()) {
                statusText.text = "Inserisci l'URL del sito"
                return@setOnClickListener
            }
            
            if (rememberCheckbox.isChecked) {
                sharedPref.edit().apply {
                    putString("url", url)
                    putString("username", usernameInput.text.toString())
                    putString("password", passwordInput.text.toString())
                    putBoolean("remember", true)
                    apply()
                }
            }
            
            statusText.text = "Connesso al sito"
            searchButton.isEnabled = true
        }
        
        // Ricerca film
        searchButton.isEnabled = false
        searchButton.setOnClickListener {
            val searchTitle = searchInput.text.toString()
            if (searchTitle.isEmpty()) {
                statusText.text = "Inserisci il titolo del film"
                return@setOnClickListener
            }
            
            statusText.text = "Ricerca in corso..."
            scope.launch {
                try {
                    val siteUrl = urlInput.text.toString()
                    val results = searchMovies(siteUrl, searchTitle)
                    
                    val adapter = ArrayAdapter(
                        this@MainActivity,
                        android.R.layout.simple_list_item_1,
                        results.map { it.title }
                    )
                    resultsListView.adapter = adapter
                    
                    resultsListView.setOnItemClickListener { _, _, position, _ ->
                        val selectedResult = results[position]
                        statusText.text = "Caricamento magnet link..."
                        scope.launch {
                            try {
                                val magnetLink = extractMagnetLink(selectedResult.link)
                                if (magnetLink != null) {
                                    statusText.text = "Inizio download: ${selectedResult.title}"
                                    startTorrentStream(magnetLink, selectedResult.title)
                                } else {
                                    statusText.text = "Errore: magnet link non trovato"
                                }
                            } catch (e: Exception) {
                                statusText.text = "Errore: ${e.message}"
                            }
                        }
                    }
                    
                    statusText.text = "Trovati ${results.size} risultati"
                } catch (e: Exception) {
                    statusText.text = "Errore ricerca: ${e.message}"
                }
            }
        }
    }
    
    private suspend fun searchMovies(siteUrl: String, title: String): List<SearchResult> {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val searchUrl = "$siteUrl/search?q=$title"
                val request = Request.Builder().url(searchUrl).build()
                val response = client.newCall(request).execute()
                
                val doc = Jsoup.parse(response.body?.string() ?: "")
                val results = mutableListOf<SearchResult>()
                
                // Estrai i link dai risultati (adatta il selettore CSS al tuo sito)
                doc.select("a[href*=/torrent/]").forEach { element ->
                    val title = element.text()
                    val link = element.attr("href")
                    if (title.isNotEmpty() && link.isNotEmpty()) {
                        results.add(SearchResult(title, link))
                    }
                }
                
                results
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    private suspend fun extractMagnetLink(pageUrl: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(pageUrl).build()
                val response = client.newCall(request).execute()
                
                val doc = Jsoup.parse(response.body?.string() ?: "")
                
                // Estrai il magnet link (adatta al tuo sito)
                val magnetElement = doc.select("a[href^=magnet:]").firstOrNull()
                magnetElement?.attr("href")
            } catch (e: Exception) {
                null
            }
        }
    }
    
    private fun startTorrentStream(magnetLink: String, title: String) {
        // Placeholder per lo streaming torrent
        // Questa parte richiede libtorrent4j con configurazione avanzata
        Toast.makeText(this, "Streaming torrent: $title\nMagnet: $magnetLink", Toast.LENGTH_LONG).show()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
        scope.cancel()
    }
    
    data class SearchResult(val title: String, val link: String)
}
