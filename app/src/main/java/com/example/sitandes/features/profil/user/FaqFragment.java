package com.example.sitandes.features.profil.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.sitandes.R;

public class FaqFragment extends Fragment {

    private ImageView btnBack;
    private LinearLayout[] faqItems;
    private TextView[] answers;
    private ImageView[] expandIcons;
    private boolean[] isExpanded;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_faq, container, false);

        initViews(view);
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btn_back_faq);

        // Initialize FAQ items
        faqItems = new LinearLayout[7];
        answers = new TextView[7];
        expandIcons = new ImageView[7];
        isExpanded = new boolean[7];

        for (int i = 0; i < 7; i++) {
            int itemId = getResources().getIdentifier("faq_item_" + (i + 1), "id", getActivity().getPackageName());
            int answerId = getResources().getIdentifier("answer_" + (i + 1), "id", getActivity().getPackageName());
            int iconId = getResources().getIdentifier("icon_expand_" + (i + 1), "id", getActivity().getPackageName());

            faqItems[i] = view.findViewById(itemId);
            answers[i] = view.findViewById(answerId);
            expandIcons[i] = view.findViewById(iconId);
            isExpanded[i] = false;
        }
    }

    private void setupClickListeners() {
        // Back button listener
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        // FAQ items click listeners
        for (int i = 0; i < faqItems.length; i++) {
            final int index = i;
            faqItems[i].setOnClickListener(v -> toggleFAQItem(index));
        }
    }

    private void toggleFAQItem(int index) {
        if (isExpanded[index]) {
            // Collapse
            answers[index].setVisibility(View.GONE);
            expandIcons[index].setRotation(0);
            isExpanded[index] = false;
        } else {
            // Expand
            answers[index].setVisibility(View.VISIBLE);
            expandIcons[index].setRotation(180);
            isExpanded[index] = true;
        }
    }
}