package de.juliusawen.coastercreditcounter.presentation.fragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import de.juliusawen.coastercreditcounter.R;
import de.juliusawen.coastercreditcounter.globals.Constants;
import de.juliusawen.coastercreditcounter.globals.enums.ButtonFunction;
import de.juliusawen.coastercreditcounter.presentation.BaseActivityViewModel;
import de.juliusawen.coastercreditcounter.toolbox.DrawableTool;

public class HelpOverlayFragment extends Fragment
{
    private BaseActivityViewModel viewModel;

    private TextView textViewTitle;
    private TextView textViewMessage;

    private HelpOverlayFragmentInteractionListener helpOverlayFragmentInteractionListener;

    public HelpOverlayFragment() {}

    public static HelpOverlayFragment newInstance(String helpTitle, CharSequence helpMessage)
    {
        Log.d(Constants.LOG_TAG, "HelpOverlayFragment.newInstance:: instantiating fragment...");

        HelpOverlayFragment helpOverlayFragment = new HelpOverlayFragment();
        Bundle args = new Bundle();
        args.putCharSequence(Constants.FRAGMENT_ARG_HELP_TITLE, helpTitle);
        args.putCharSequence(Constants.FRAGMENT_ARG_HELP_MESSAGE, helpMessage);
        helpOverlayFragment.setArguments(args);
        return helpOverlayFragment;
    }

    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        Log.v(Constants.LOG_TAG, "HelpOverlayFragment.onCreate:: creating fragment...");

        super.onCreate(savedInstanceState);

        this.viewModel = ViewModelProviders.of(this).get(BaseActivityViewModel.class);

        if (getArguments() != null)
        {
            this.viewModel.helpOverlayFragmentTitle = getArguments().getString(Constants.FRAGMENT_ARG_HELP_TITLE);
            if(this.viewModel.helpOverlayFragmentTitle == null)
            {
                this.viewModel.helpOverlayFragmentTitle = getString(R.string.title_help, getString(R.string.help_title_not_available));
            }

            this.viewModel.helpOverlayFragmentMessage = getArguments().getCharSequence(Constants.FRAGMENT_ARG_HELP_MESSAGE);
            if(this.viewModel.helpOverlayFragmentMessage == null)
            {
                this.viewModel.helpOverlayFragmentMessage = getString(R.string.help_text_not_available);
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.v(Constants.LOG_TAG, "HelpOverlayFragment.onCreateView:: creating view...");

        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.fragment_help_overlay, container, false);
        this.textViewTitle = linearLayout.findViewById(R.id.textViewHelp_Title);
        this.textViewMessage = linearLayout.findViewById(R.id.textViewHelp_Message);
        return linearLayout;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        Log.v(Constants.LOG_TAG, "HelpOverlayFragment.onViewCreated:: decorating view...");

        super.onViewCreated(view, savedInstanceState);

        this.textViewTitle.setText(this.viewModel.helpOverlayFragmentTitle);
        this.textViewMessage.setText(this.viewModel.helpOverlayFragmentMessage);

        ImageButton buttonBack = view.findViewById(R.id.imageButtonHelp_Close);
        Drawable drawable = DrawableTool.getColoredDrawable(R.drawable.ic_baseline_close, R.color.white);
        buttonBack.setImageDrawable(drawable);
        buttonBack.setId(ButtonFunction.CLOSE.ordinal());
        buttonBack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                HelpOverlayFragment.this.onCloseButtonPressed(view);
            }
        });
    }

    private void onCloseButtonPressed(View view)
    {
        if (this.helpOverlayFragmentInteractionListener != null)
        {
            this.helpOverlayFragmentInteractionListener.onHelpOverlayFragmentInteraction(view);
        }
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        if (context instanceof HelpOverlayFragmentInteractionListener)
        {
            this.helpOverlayFragmentInteractionListener = (HelpOverlayFragmentInteractionListener) context;
        }
        else
        {
            throw new RuntimeException(context.toString() + " must implement HelpOverlayFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        this.textViewTitle = null;
        this.textViewMessage = null;
        this.helpOverlayFragmentInteractionListener = null;
    }

    public interface HelpOverlayFragmentInteractionListener
    {
        void onHelpOverlayFragmentInteraction(View view);
    }
}
