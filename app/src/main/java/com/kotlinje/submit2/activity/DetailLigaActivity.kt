package com.kotlinje.submit2.activity

import android.database.sqlite.SQLiteConstraintException
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.kotlinje.submit2.R
import com.kotlinje.submit2.model.event.EventLiga
import com.kotlinje.submit2.model.event.ModelFavorite
import com.kotlinje.submit2.model.event.ModelTeam
import com.kotlinje.submit2.model.event.ModelTeamItem
import com.kotlinje.submit2.network.repository.DetailRepository
import com.kotlinje.submit2.presenter.PresenterDetail
import com.kotlinje.submit2.view.DetailView
import kotlinx.android.synthetic.main.activity_detail.*
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.toast
import com.kotlinje.submit2.utility.database
import org.jetbrains.anko.db.classParser
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.select
class DetailLigaActivity : AppCompatActivity(), DetailView {
    override fun onDataLoaded(data: ModelTeam?) {
        showToastDetail("test data : $data")
    }

    override fun onDataError() {
        showToastDetail("test data error")
    }

    private fun addToFavorite() {


        try {
            database.use {
                insert(ModelFavorite.TABLE_FAVORITE,
                        ModelFavorite.EVENT_ID to eventLiga?.idEvent,
                        ModelFavorite.EVENT_DATE to eventLiga?.dateEvent,
                        ModelFavorite.HOME_TEAM to eventLiga?.strHomeTeam,
                        ModelFavorite.AWAY_TEAM to eventLiga?.strAwayTeam,
                        ModelFavorite.SCORE_HOME to eventLiga?.intHomeScore,
                        ModelFavorite.SCORE_AWAY to eventLiga?.intAwayScore
                )
            }

            showToastDetail("data berhasil disimpan")
        } catch (e: SQLiteConstraintException) {
            showToastDetail("Error Insert" + e.localizedMessage)

        }
    }

    // remove berfungsi untuk delete data
    private fun removeFromFavorite() {
        try {
            database.use {

                delete(ModelFavorite.TABLE_FAVORITE,
                        "(EVENT_ID = {id})", "id" to idEvent)

            }
            showToastDetail("hapus dari favorit")
        } catch (e: SQLiteConstraintException) {
            showToastDetail("error hapus :" + e.localizedMessage)
//
        }
    }

    //
    private fun setFavorite(isFav: Boolean) {
        if (isFav) {

            menuItemDel?.getItem(0)?.icon =
                    ContextCompat.getDrawable(this, R.drawable.ic_favorite_black_24dp)
        } else {
            menuItemDel?.getItem(0)?.icon =
                    ContextCompat.getDrawable(this, R.drawable.ic_favorite_border_black_24dp)
        }
    }

    private fun cekFavor() {
        try {
            database.use {
                //query select id
                val hasilData = select(ModelFavorite.TABLE_FAVORITE)
                        .whereArgs("(EVENT_ID = {id})", "id" to idEvent)
                val eventFavoriteTeam = hasilData.parseList(classParser<ModelFavorite>())

                isEvenFavor = !eventFavoriteTeam.isEmpty()
                //set favorite match team
                setFavorite(isEvenFavor)
            }

        } catch (e: SQLiteConstraintException) {
            showToastDetail("Error Cek Favorit $e.localizedMessage")
        }
    }

    override fun showHomeTeamImg(team: ModelTeamItem?) {
        val imgHome = team?.strTeamBadge
        Glide.with(this).load(imgHome).into(imgHomeTeam)
    }

    override fun showAwayTeamImg(team: ModelTeamItem?) {
        val imgAway = team?.strTeamBadge
        Glide.with(this).load(imgAway).into(imgAwayTeam)

    }

    override fun showLoadingProgress() {
        load?.visibility = View.VISIBLE

    }

    override fun hideLoadingProgress() {
        load?.visibility = View.INVISIBLE
    }

    override fun showToastDetail(message: String?) {
        toast(message.toString())
    }

     override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_detai_activity, menu)
        menuItemDel = menu
        setFavorite(isEvenFavor)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.favoriteteam -> {
                // kondisi pada menu item
                // remove / delete
                if (isEvenFavor) {
                    removeFromFavorite()
                    isEvenFavor = false
                } else {
                    // add to favorite
                    addToFavorite()
                    isEvenFavor = true
                }

                //set favorite
                setFavorite(isEvenFavor)
                true
            }
            else -> super.onOptionsItemSelected(item)

        }
    }

    //show event liga
    override fun showEventLiga(event: EventLiga?) {
        eventLiga = event
        idHomeTeam = event?.idHomeTeam
        idAwayTeam = event?.idAwayTeam
        textScoreHomeTeam.text = event?.intHomeScore
        textScoreAwayTeam.text = event?.intAwayScore
        textGoalHomeTeam.text = event?.strHomeGoalDetails
        tvGoalAwayTeam.text = event?.strAwayGoalDetails

        idHome = idHomeTeam
        idAway = idAwayTeam
        present?.getImgHome(idHome.toString())
        present?.getImgAway(idAway.toString())

     }

    //variabel
    var idHome: String? = null
    var idAway: String? = null
    private var isEvenFavor: Boolean = false
    var load: ProgressBar? = null
    var present: PresenterDetail? = null
    var idHomeTeam: String? = null
    var idAwayTeam: String? = null
    var eventLiga: EventLiga? = null
    lateinit var imgHomeTeam: ImageView
    lateinit var imgAwayTeam: ImageView
    lateinit var textScoreHomeTeam: TextView
    lateinit var textScoreAwayTeam: TextView
    lateinit var textGoalHomeTeam: TextView
    lateinit var tvGoalAwayTeam: TextView
    lateinit var idEvent: String
    private var menuItemDel: Menu? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)


        //get intent
        idEvent = intent.getStringExtra("idEvent")
        present = PresenterDetail(this, DetailRepository())
        present?.getMatchLast(idEvent)
        imgHomeTeam = img_home_team
        imgAwayTeam = img_away_team
        textScoreHomeTeam = text_score_home_team
        textScoreAwayTeam = text_score_away_team
        textGoalHomeTeam = text_goal_home_team
        tvGoalAwayTeam = text_goal_away_team

        // pada oncrete method dicek dulu favoritenya,
        // sehingga pada menu item icon akan terganti
        cekFavor();


    }
}
