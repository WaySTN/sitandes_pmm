package com.example.sitandes.features.profil.admin;

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

public class PengaturanAdminFragment extends Fragment {

    private ImageView btnBack;
    private LinearLayout[] pengaturanItems;
    private TextView[] answers;
    private ImageView[] expandIcons;
    private boolean[] isExpanded;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pengaturan_admin, container, false);

        initViews(view);
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btn_back_pengaturan);

        // Initialize pengaturan items
        pengaturanItems = new LinearLayout[4];
        answers = new TextView[4];
        expandIcons = new ImageView[4];
        isExpanded = new boolean[4];

        for (int i = 0; i < 4; i++) {
            int itemId = getResources().getIdentifier("pengaturan_item_" + (i + 1), "id", getActivity().getPackageName());
            int answerId = getResources().getIdentifier("answer_" + (i + 1), "id", getActivity().getPackageName());
            int iconId = getResources().getIdentifier("icon_expand_" + (i + 1), "id", getActivity().getPackageName());

            pengaturanItems[i] = view.findViewById(itemId);
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

        // Pengaturan items click listeners
        for (int i = 0; i < pengaturanItems.length; i++) {
            final int index = i;
            pengaturanItems[i].setOnClickListener(v -> togglePengaturanItem(index));
        }
    }

    private void togglePengaturanItem(int index) {
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