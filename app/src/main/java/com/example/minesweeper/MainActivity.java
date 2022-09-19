package com.example.minesweeper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.os.Handler;
import java.util.Random;
import java.util.HashSet;
import java.util.Stack;
import android.widget.EditText;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // save the TextViews of all cells in an array, so later on,
    // when a TextView is clicked, we know which cell it is
    private ArrayList<TextView> cell_tvs;

    //for timer
    private int clock = 0;
    private boolean running = true;
    private HashSet<Integer> bombs = new HashSet<Integer>();
    private int flags = 4;
    private boolean alive = true;
    private boolean pick = true;
    private String values[];


    private int dpToPixel(int dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cell_tvs = new ArrayList<TextView>();

        GridLayout grid = (GridLayout) findViewById(R.id.gridLayout01);
        for (int i = 0; i<=9; i++) {
            for (int j = 0; j <= 7; j++) {
                TextView tv = new TextView(this);
                tv.setHeight(dpToPixel(32));
                tv.setWidth(dpToPixel(32));
                tv.setTextSize(16);
                tv.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                tv.setTextColor(Color.GREEN);
                tv.setBackgroundColor(Color.GREEN);
                tv.setOnClickListener(this::onClickTV);
                GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
                lp.setMargins(dpToPixel(2), dpToPixel(2), dpToPixel(2), dpToPixel(2));
                lp.rowSpec = GridLayout.spec(i);
                lp.columnSpec = GridLayout.spec(j);
                grid.addView(tv, lp);
                cell_tvs.add(tv);
            }
        }
        //for array
        values = new String[cell_tvs.size()];

        //for timer
        if (savedInstanceState != null) {
            clock = savedInstanceState.getInt("clock");
            running = savedInstanceState.getBoolean("running");
        }

        runTimer();

        //for placing the bombs
        int bound = cell_tvs.size();
        Random rand = new Random();
        while(bombs.size() < 4){
            int random = rand.nextInt(bound);
            if(!bombs.contains(random)){
                bombs.add(random);
            }
        }

        //for the flag and pick
        final TextView inter = (TextView) findViewById(R.id.interchange);
        inter.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View view){
                String check = inter.getText().toString();
                if(check.equals("\uD83D\uDEA9")){
                    inter.setText(R.string.pick);
                    pick = true;
                }else{
                    inter.setText(R.string.flag);
                    pick = false;
                }
            }
        });

        //for placing the numbers on the grid
        for(int current = 0; current < cell_tvs.size(); current++){
            int counter = 0;
            //0th col edge cases
            if(current%8 > 0){
                if(current-9 >= 0) {
                    if (bombs.contains(current - 9)) {
                        counter++;
                    }
                }
                if(current-1 >= 0) {
                    if (bombs.contains(current - 1)) {
                        counter++;
                    }
                }
                if(current+7 <= 79) {
                    if (bombs.contains(current +7)) {
                        counter++;
                    }
                }
            }
            //7th col edge cases
            if(current%8 < 7){
                if(current+9 <= 79) {
                    if (bombs.contains(current + 9)) {
                        counter++;
                    }
                }
                if(current+1 <= 79) {
                    if (bombs.contains(current + 1)) {
                        counter++;
                    }
                }
                if(current-7 >=0) {
                    if (bombs.contains(current -7)) {
                        counter++;
                    }
                }
            }
            //for 0th row edge cases
            if(current - 8 >= 0){
                if (bombs.contains(current -8)) {
                    counter++;
                }
            }
            //for 9th row edge cases
            if(current + 8 <= 79){
                if (bombs.contains(current +8)) {
                    counter++;
                }
            }
            if (counter > 0) {
                cell_tvs.get(current).setText(String.valueOf(counter));
                values[current] = String.valueOf(counter);
            }else if(counter == 0){
                values[current] = "";
            }
        }
    }

    private int findIndexOfCellTextView(TextView tv) {
        for (int n=0; n<cell_tvs.size(); n++) {
            if (cell_tvs.get(n) == tv)
                return n;
        }
        return -1;
    }

    //sendMessage
    public void sendMessage(){
        String message;
        if(alive){
            message = clock + " seconds." + "\n" + "You won." + "\n" + "Good job!";
        }else{
            message = clock + " seconds." + "\n" + "You lost." + "\n" + "Try again!";
        }

        Intent intent = new Intent(this, ResultsActivity.class);
        intent.putExtra("com.example.sendmessage.MESSAGE", message);

        startActivity(intent);
    }

    //when clicking a cell
    public void onClickTV(View view){
        //when game over
        if(!running) {
            sendMessage();
            return;
        }

        TextView tv = (TextView) view;
        int loc = findIndexOfCellTextView(tv);
        //if safe space
        if(!bombs.contains(loc) && tv.getCurrentTextColor() == Color.GREEN && pick && !tv.getText().equals("\uD83D\uDEA9")){
            if(!cell_tvs.get(loc).getText().equals("1") && !cell_tvs.get(loc).getText().equals("2")
                    && !cell_tvs.get(loc).getText().equals("3") && !cell_tvs.get(loc).getText().equals("4")){
                dfs(loc);
            }else{
                tv.setTextColor(Color.BLACK);
                tv.setBackgroundColor(Color.LTGRAY);
            }
            //win condition
            int win = 0;
            for(int i = 0; i < cell_tvs.size(); i++){
                if(cell_tvs.get(i).getCurrentTextColor() == Color.GREEN){
                    win++;
                }
            }
            if(win == 4){
                running = false;
                alive = true;
                for(int iterate: bombs){// reveal bomb
                    cell_tvs.get(iterate).setText(R.string.mine);
                }
            }
        }//if bomb
        else if(bombs.contains(loc) && pick && !tv.getText().equals("\uD83D\uDEA9")){
            alive = false;
            running = false;
            tv.setBackgroundColor(Color.LTGRAY);
            tv.setText(R.string.mine);
            for(int iterate: bombs){// reveal bomb
                cell_tvs.get(iterate).setBackgroundColor(Color.LTGRAY);
                cell_tvs.get(iterate).setText(R.string.mine);
            }
        }//when in the flag mode (NOT WORKING)
        else if(!pick){
            final TextView flagView = (TextView) findViewById(R.id.textView04);
            if(tv.getCurrentTextColor() == Color.GREEN && !tv.getText().equals("\uD83D\uDEA9")){
                tv.setText(R.string.flag);
                flags--;
            }else if(tv.getCurrentTextColor() == Color.GREEN && tv.getText().equals("\uD83D\uDEA9")){
                tv.setText(values[loc]);
                flags++;
            }
            flagView.setText(String.valueOf(flags));
        }
    }

    //for timer
    private void runTimer() {
        final TextView timeView = (TextView) findViewById(R.id.textView);
        final Handler handler = new Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {
                int seconds = clock;
                String time = String.format("%02d", seconds);
                timeView.setText(time);
                if (running) {
                    clock++;
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    //for dfs
    private void dfs(int square){
        Stack<Integer> stack = new Stack<Integer>();//stack for cells needed to be checked
        HashSet<Integer> visited = new HashSet<Integer>();//set for visited cells
        stack.push(square);
        visited.add(square);
        while(!stack.empty()) {
            int current = stack.pop();//remove and store element from top of stack
            if (bombs.contains(current)) { //if square is a bomb then continue
                continue;
            } else if (cell_tvs.get(current).getCurrentTextColor() == Color.BLACK) { //if square is already revealed
                continue;
            }
            //reveal the square
            if(!cell_tvs.get(current).getText().equals("\uD83D\uDEA9")){
                cell_tvs.get(current).setTextColor(Color.BLACK);
                cell_tvs.get(current).setBackgroundColor(Color.LTGRAY);
            }

            //pushing to the stack if we are not at a number
            if(!cell_tvs.get(current).getText().equals("1") && !cell_tvs.get(current).getText().equals("2")
                    && !cell_tvs.get(current).getText().equals("3") && !cell_tvs.get(current).getText().equals("4")){
                if (!visited.contains(current - 9) && current - 9 >= 0) {
                    stack.push(current - 9);
                    visited.add(current - 9);
                }
                if (!visited.contains(current - 1) && current - 1 >= 0) {
                    stack.push(current - 1);
                    visited.add(current - 1);
                }
                if (!visited.contains(current - 8) && current - 8 >= 0) {
                    stack.push(current - 8);
                    visited.add(current - 8);
                }
                if (!visited.contains(current - 7) && current - 7 >= 0) {
                    stack.push(current - 7);
                    visited.add(current - 7);
                }
                if (!visited.contains(current + 7) && current + 7 <= 79) {
                    stack.push(current + 7);
                    visited.add(current + 7);
                }
                if (!visited.contains(current + 9) && current + 9 <= 79) {
                    stack.push(current + 9);
                    visited.add(current + 9);
                }
                if (!visited.contains(current + 8) && current + 8 <= 79) {
                    stack.push(current + 8);
                    visited.add(current + 8);
                }
                if (!visited.contains(current + 1) && current + 1 <= 79) {
                    stack.push(current + 1);
                    visited.add(current + 1);
                }
            }
        }
    }
}