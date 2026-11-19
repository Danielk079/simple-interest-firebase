package com.danielk.simpleinterest;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    // Business rule (assumption): loans longer than 10 years carry a 0.12
    // percentage-point premium on the rate, applied to the whole loan period.
    private static final double LONG_LOAN_YEARS = 10;
    private static final double LONG_LOAN_RATE_PREMIUM = 0.12;

    private EditText etPrincipal, etRate, etTime, etInterest;
    private Button btnCalculate;
    private DatabaseReference loansRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etPrincipal = findViewById(R.id.etPrincipal);
        etRate = findViewById(R.id.etRate);
        etTime = findViewById(R.id.etTime);
        etInterest = findViewById(R.id.etInterest);
        btnCalculate = findViewById(R.id.btnCalculate);

       // everything we push will be stored under a "loans" node
        loansRef = FirebaseDatabase.getInstance().getReference("loans");

        btnCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateAndSave();
            }
        });
    }

    private void calculateAndSave() {
        String pText = etPrincipal.getText().toString().trim();
        String rText = etRate.getText().toString().trim();
        String tText = etTime.getText().toString().trim();

        if (pText.isEmpty() || rText.isEmpty() || tText.isEmpty()) {
            Toast.makeText(this, "Please fill in Principal, Rate and Time", Toast.LENGTH_SHORT).show();
            return;
        }

        double principal, rate, time;
        try {
            principal = Double.parseDouble(pText);
            rate = Double.parseDouble(rText);
            time = Double.parseDouble(tText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            return;
        }

        if (principal <= 0 || rate <= 0 || time <= 0) {
            Toast.makeText(this, "Values must be greater than zero", Toast.LENGTH_SHORT).show();
            return;
        }

        // apply the long-loan premium to the rate if the loan is over 10 years
        double effectiveRate = rate;
        if (time > LONG_LOAN_YEARS) {
            effectiveRate = rate + LONG_LOAN_RATE_PREMIUM;
            Toast.makeText(this, "Over 10 years: 0.12% rate premium applied", Toast.LENGTH_SHORT).show();
        }

        // simple interest = P x R x T / 100, rounded to 2 decimal places
        double interest = (principal * effectiveRate * time) / 100;
        interest = Math.round(interest * 100.0) / 100.0;

        // 1) display the interest first
        etInterest.setText(String.format(Locale.US, "%.2f", interest));

        // 2) then push everything to Firebase
        Map<String, Object> loan = new HashMap<>();
        loan.put("principal", principal);
        loan.put("rate", rate);
        loan.put("time", time);
        loan.put("interest", interest);

        loansRef.push().setValue(loan)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(MainActivity.this, "Saved to Firebase", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(MainActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}