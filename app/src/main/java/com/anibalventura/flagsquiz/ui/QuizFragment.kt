package com.anibalventura.flagsquiz.ui

import android.app.AlertDialog
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.anibalventura.flagsquiz.R
import com.anibalventura.flagsquiz.Utils
import com.anibalventura.flagsquiz.data.local.*
import com.anibalventura.flagsquiz.databinding.FragmentQuizBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QuizFragment : Fragment() {

    // Use DataBinding.
    private lateinit var binding: FragmentQuizBinding

    // Flags.
    private lateinit var flags: MutableList<Flags> // Flags from database.
    private lateinit var currentFlag: Flags
    private var indexFlag: Int = 0
    private var submitFlag: Boolean = false
    private val numFlags = 20

    // Answers.
    private lateinit var answers: MutableList<String>
    private lateinit var answersView: MutableList<TextView>
    private var indexAnswer: Int = 0
    private var correctAnswers: Int = 0
    private val indexo: Int = 0

    // Lives count.
    private var lives: Int = 5

    /**
     * Using LiveData and caching what getHistory returns has several benefits:
     * 1. Put an observer on the data (instead of polling for changes) and only update the
     *    the UI when the data actually changes.
     * 2. Repository is completely separated from the UI through the ViewModel.
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*
         * Get Room Database.
         */

    }

    /**
     * Launching a new coroutine to insert the data in a non-blocking way.
     */


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        /**
         * Inflate the layout for this fragment.
         */
        // Use DataBindingUtil.inflate to inflate and return the Fragment in onCreateView.
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_quiz, container, false)
        // Specify the fragment view as the lifecycle owner of the binding.
        // This is used so that the binding can observe LiveData updates.
        binding.lifecycleOwner = viewLifecycleOwner



        binding.retour.setOnClickListener {
            findNavController()
                .navigate(QuizFragmentDirections.actionQuizFragmentToHomeFragment())
        }

        /*
         * Initiate the quiz.
         */
        // Get answer views.
        answersView = mutableListOf(
            binding.tvAnswerOne,
            binding.tvAnswerTwo,
            binding.tvAnswerThree,
            binding.tvAnswerFour
        )

        // Get selected continent for flags.
        selectedContinent()

        // Submit the answer.


        return binding.root
    }

    /*
     * Get selected continent from HomeFragment with SafeArgs.
     * And set title fragment.
     */
    private fun selectedContinent() {
        val args = QuizFragmentArgs.fromBundle(requireArguments())
        when (args.continent) {
            getString(R.string.continent_africa) -> {
                flags = Africa.getFlags()
                (activity as AppCompatActivity).supportActionBar?.title =
                    getString(R.string.continent_africa)
            }
            getString(R.string.continent_asia) -> {
                flags = Asia.getFlags()
                (activity as AppCompatActivity).supportActionBar?.title =
                    getString(R.string.continent_asia)
            }
            getString(R.string.continent_europe) -> {
                flags = Europe.getFlags()
                (activity as AppCompatActivity).supportActionBar?.title =
                    getString(R.string.continent_europe)
            }
            getString(R.string.continent_north_america) -> {
                flags = NorthAmerica.getFlags()
                (activity as AppCompatActivity).supportActionBar?.title =
                    getString(R.string.continent_north_america)
            }
            getString(R.string.continent_oceania) -> {
                flags = Oceania.getFlags()
                (activity as AppCompatActivity).supportActionBar?.title =
                    getString(R.string.continent_oceania)
            }
            getString(R.string.continent_south_america) -> {
                flags = SouthAmerica.getFlags()
                (activity as AppCompatActivity).supportActionBar?.title =
                    getString(R.string.continent_south_america)
            }
        }
        // Set flag from selected continent.
        setFlag()
        // Update answer view on user selection.
        selectedAnswerView()
    }

    /**
     * Setting the flag to UI components.
     */
    private fun setFlag() {
        // Set random flag and answer.
        randomFlagAndAnswer()

        // Reset the view to default.
        defaultAnswerView()

        // Update the progress bar.
        binding.progressBar.progress = indexFlag.plus(1)
        binding.progressBar.max = numFlags
        binding.tvProgressBar.text =
            getString(R.string.quiz_progress, indexFlag.plus(1), numFlags)

        // Update the view to the current flag.
        binding.ivFlag.setImageResource(currentFlag.image)
        for ((index, answer) in answersView.withIndex()) {
            answer.text = answers[index]
            // Enable to select an answer.
            answer.isClickable = true
        }

        // Update button view to "Submit".


        // Cannot submit flag if a answer is not selected.
        submitFlag = false
    }

    /*
     * Random flag and answers orders.
     */
    private fun randomFlagAndAnswer() {
        // Set random order for flags.
        flags.shuffle()
        // Update current flag.
        currentFlag = flags[indexFlag]
        // Set answers into a copy of the mutableList.
        answers = currentFlag.answers.toMutableList()
        // Set random order for answers.
        answers.shuffle()
    }

    /*
     * Update answers view.
     */
    // Set the view of answersView to default.
    private fun defaultAnswerView() {
        for (answer in answersView) {
            answer.setTextColor(ContextCompat.getColor(answer.context, R.color.secondaryTextColor))
            answer.typeface = Typeface.DEFAULT
            answer.background = ContextCompat.getDrawable(
                requireContext(), R.drawable.bg_default_answer_border
            )
        }
    }


    // Get the selected index answer and set the view.
    private fun selectedAnswerView() {
        for ((index, answer) in answersView.withIndex()) {
            answer.setOnClickListener {
                defaultAnswerView()

                // Set the answer view to selected.
                answer.setTypeface(answer.typeface, Typeface.BOLD)
                answer.background =
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.bg_selected_answer_border

                    )

                // Get the selected answer.

                indexAnswer = index
                // Enable to select an answer.
                answer.isClickable = true
                // Can pass to another flag.
                submitFlag = true
                submitAnswer()
            }
        }
    }
    //function tkharbi9a
    //show popUp window in case of incorrecte answer
    private fun showPopUpInCorrecteAnswer(){
        var dialog: AlertDialog? = null
        val alertdialogue = AlertDialog.Builder(requireContext())
        val modal_Arreter: View = layoutInflater.inflate(R.layout.rong_answer_pop_up, null)
        alertdialogue.setView(modal_Arreter)
        dialog = alertdialogue.create()
        dialog.show()
        modal_Arreter.findViewById<ImageView>(R.id.okrong)
            .setOnClickListener(View.OnClickListener { dialog.cancel()
                submitAnswer()})

    }
    //show popUp window in case of game over
    private fun showPopUpgameOver(){
        var dialog: AlertDialog? = null
        val alertdialogue = AlertDialog.Builder(requireContext())
        val modal_Arreter: View = layoutInflater.inflate(R.layout.game_over, null)
        alertdialogue.setView(modal_Arreter)
        dialog = alertdialogue.create()
        dialog.show()
        modal_Arreter.findViewById<ImageView>(R.id.okover)
            .setOnClickListener(View.OnClickListener { dialog.cancel()
                submitAnswer()})

    }
    //show popUp window in case of player lose the game
    private fun showPopUpLoseGame(){
        var dialog: AlertDialog? = null
        val alertdialogue = AlertDialog.Builder(requireContext())
        val modal_Arreter: View = layoutInflater.inflate(R.layout.lose_test, null)
        alertdialogue.setView(modal_Arreter)
        dialog = alertdialogue.create()
        dialog.show()
        modal_Arreter.findViewById<ImageView>(R.id.oklose)
            .setOnClickListener(View.OnClickListener { dialog.cancel()
                submitAnswer()})

    }
    //afficher popUp window incase of correcte answer
    private fun showPopUpCorrecteAnswer(){
        var dialog: AlertDialog? = null
        val alertdialogue = AlertDialog.Builder(requireContext())
        val modal_Arreter: View = layoutInflater.inflate(R.layout.correcte_answer_pop_up, null)
        alertdialogue.setView(modal_Arreter)
        dialog = alertdialogue.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()


        //val window: Window? = dialog.getWindow()
        //window?.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT)

        modal_Arreter.findViewById<ImageView>(R.id.okcorrecte)
            .setOnClickListener(View.OnClickListener { dialog.cancel()
                submitAnswer()})

    }


    // Highlight the answer selected for wrong or right.
    private fun highlightAnswerView(answer: String, drawableView: Int) {
        for ((index) in answersView.withIndex()) {
            when (answer) {
                answers[index] -> {
                    // Set text color.
                    answersView[index].setTextColor(
                        ContextCompat.getColor(
                            answersView[index].context,
                            R.color.primaryTextColor
                        )
                    )

                    // Set background.
                    answersView[index].background =
                        ContextCompat.getDrawable(requireContext(), drawableView)
                }
            }
        }
    }


    /*
     * Submit the answer.
     */
    private fun submitAnswer() {

        val args = QuizFragmentArgs.fromBundle(requireArguments())

        when {
            // When out of live, go to QuizOverFragment.
            lives <= 0 -> {
                // Insert data to database.

                if (correctAnswers > 3) {

                    findNavController().navigate(
                        QuizFragmentDirections.actionQuizFragmentToWonFragment(
                            correctAnswers, numFlags, args.continent
                        )
                    )
                } else {
                    findNavController()
                        .navigate(QuizFragmentDirections.actionQuizFragmentToLoseFragment(args.continent))

                }
            }

            // Show a toast if trying to submit flag without an answer.
            !submitFlag -> Utils.showToast(
                requireContext(),
                getString(R.string.quiz_select_answer)
            )

            // When pass to another flag.
            indexAnswer == 5 -> { // The number 5 have no value, is only used to continue the quiz.
                // Update index to pass to another flag.
                indexFlag++

                when {
                    // If number of flags answered not meet the total flags, continue the quiz.
                    indexFlag < numFlags -> {
                        // Set a new flag.
                        setFlag()
                        // Update selected answer view.
                        selectedAnswerView()
                    }
                    else -> {
                        // Insert data to database.


                        // Go to QuizWonFragment when finish the quiz and pass arguments.
                        findNavController().navigate(
                            QuizFragmentDirections.actionQuizFragmentToWonFragment(
                                correctAnswers, numFlags, args.continent
                            )
                        )
                    }
                }
            }

            // When answer selected check for correct or wrong answer.
            else -> {
                // If answer wrong.
                if (answers[indexAnswer] != currentFlag.answers[0]) {
                    // Mark selected view to wrong.
                    showPopUpInCorrecteAnswer()

                    // Decrease lives.
                    lives--
                    // Hide livesView when wrong answer.
                    when (lives) {
                        5 -> binding.ivLiveFive.visibility = View.GONE
                        4 -> binding.ivLiveFour.visibility = View.GONE
                        3 -> binding.ivLiveFour.visibility = View.GONE
                        2 -> binding.ivLiveOne.visibility = View.GONE
                        1 -> binding.ivLiveTwo.visibility = View.GONE
                        0 -> binding.ivLiveThree.visibility = View.GONE
                    }
                } else {
                    correctAnswers++
                    showPopUpCorrecteAnswer()
                }

                // Always check for correct answer.


                // When answer is submit, user cannot change the answer.
                for (answer in answersView) {
                    answer.isClickable = true
                }



                // Reset selected answer for pass to another flag.
                indexAnswer = 5
            }
        }
    }

}