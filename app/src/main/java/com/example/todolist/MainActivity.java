package com.example.todolist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private ArrayList<Task> taskList;
    private DatabaseReference databaseReference;
    private EditText taskInput;
    private Button addButton;
    private FirebaseAuth Auth;

    public MainActivity(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Auth = FirebaseAuth.getInstance();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        taskList = new ArrayList<>();
        adapter = new TaskAdapter(taskList, new TaskAdapter.OnItemClickListener() {
            @Override
            public void onDeleteClick(int position) {
                Task itemToRemove = taskList.get(position);
                deleteItem(itemToRemove);
                taskList.remove(position);
                adapter.notifyItemRemoved(position);
            }
        });



    loadTasks();

}
    private void redirectToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void addNewTask() {
        String taskDesc = taskInput.getText().toString().trim();
        if (!taskDesc.isEmpty()) {
            String id = databaseReference.push().getKey();
            Task newTask = new Task(id, taskDesc);
            databaseReference.child(id).setValue(newTask).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Task added", Toast.LENGTH_SHORT).show();
                        taskInput.setText("");
                        taskList.add(newTask);
                        adapter.notifyItemInserted(taskList.size() - 1);
                    } else {
                        Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(MainActivity.this, "No empty spaces", Toast.LENGTH_SHORT).show();
        }
    }
    private void deleteItem(Task task) {
        databaseReference.child(task.getTaskId()).removeValue();
    }
    private void loadTasks() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                taskList.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Task task = postSnapshot.getValue(Task.class);
                    if (task != null) {
                        task.setTaskId(postSnapshot.getKey());
                        taskList.add(task);
                    }
                }
                adapter.notifyDataSetChanged();
            }


            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

