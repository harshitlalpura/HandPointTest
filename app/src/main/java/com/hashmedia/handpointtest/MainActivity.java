package com.hashmedia.handpointtest;

import android.content.Context;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.handpoint.api.HandpointCredentials;
import com.handpoint.api.Hapi;
import com.handpoint.api.HapiFactory;
import com.handpoint.api.shared.ConnectionMethod;
import com.handpoint.api.shared.ConnectionStatus;
import com.handpoint.api.shared.Currency;
import com.handpoint.api.shared.Device;
import com.handpoint.api.shared.Events;
import com.handpoint.api.shared.OperationStartResult;
import com.handpoint.api.shared.StatusInfo;
import com.handpoint.api.shared.TipConfiguration;
import com.handpoint.api.shared.TransactionResult;
import com.handpoint.api.shared.agreements.Acquirer;
import com.handpoint.api.shared.agreements.Credential;
import com.handpoint.api.shared.agreements.MerchantAuth;
import com.handpoint.api.shared.options.SaleOptions;

import java.math.BigInteger;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements Events.SmartposRequired, Events.CurrentTransactionStatus, Events.ConnectionStatusChanged, Events.EndOfTransaction, Events.TransactionResultReady {

    private Hapi api;

    Button btnPay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initApi(this);
        btnPay = (Button) findViewById(R.id.labeled);



    }

    public void initApi(Context context) {
        String sharedSecret = "C0012D1A92E48DCDC54C0BB1565339EC77695A3D2C7C7CAC60C3DF82DE037349";
        HandpointCredentials handpointCredentials = new HandpointCredentials(sharedSecret);
        this.api = HapiFactory.getAsyncInterface(this, context, handpointCredentials);
        // The api is now initialized. Yay! we've even set default credentials.
        // The shared secret is a unique string shared between the payment terminal and your application, it is unique per merchant.
        // You should replace this default shared secret with the one sent by the Handpoint support team.

        //Since we're running inside the terminal, we can create a device ourselves and connect to it
        Device device = new Device("some name", "address", "", ConnectionMethod.ANDROID_PAYMENT);
        this.api.connect(device);
    }

    @Override
    public void connectionStatusChanged(ConnectionStatus status, Device device) {
        if (status == ConnectionStatus.Connected) {
            //Connection Status connected

        }
    }

    public OperationStartResult pay() {
        return this.api.sale(new BigInteger("1000"), Currency.GBP);
        // Let´s start our first payment of 10.00 pounds
        // Use the currency of the country in which you will be deploying terminals
    }

    public OperationStartResult payWithOptions() {
        SaleOptions options = new SaleOptions();

        // Adding tipping
        TipConfiguration config = new TipConfiguration();
        //Optionally
        config.setHeaderName("HEADER");
        //Optionally
        config.setFooter("FOOTER");
        //Optionally
        config.setEnterAmountEnabled(true);
        //Optionally
        config.setSkipEnabled(true);
        //Optionally
        config.setTipPercentages(Arrays.asList(5, 10, 15, 20));
        options.setTipConfiguration(config);

        // Adding Multi MID / Custom merchant Authentication
        MerchantAuth auth = new MerchantAuth();
        Credential credential = new Credential();
        //Optionally
        credential.setAcquirer(Acquirer.SANDBOX);
        //Optionally
        credential.setMid("mid");
        //Optionally
        credential.setTid("tid");
        //Add as many credentials as Acquirers your merchant have agreements with
        auth.add(credential);
        options.setMerchantAuth(auth);

        //Add a customer reference
        options.setCustomerReference("Your customer reference");

        //Enable pin bypass
        options.setPinBypass(true);

        //Enable signature bypass
        options.setSignatureBypass(true);

        //Define a budget number
        options.setBudgetNumber("YOUR_BUDGET_NUMBER");

        return this.api.sale(new BigInteger("1000"), Currency.GBP, options);
        // Let´s start our first payment of 10.00 pounds
        // Use the currency of the country in which you will be deploying terminals
    }

    public boolean getTrxStatus() {
        //Allows you to know the status of a transaction by providing the transactionReference.
        //The transactionReference must be a unique identifier (UUID v4).
        //This functionality is only available for SmartPos devices (PAX)
        return api.getTransactionStatus("00000000-0000-0000-0000-000000000000");
        //You will receive the TransactionResult object of this operation in transactionResultReady event
    }

    @Override
    public void currentTransactionStatus(StatusInfo statusInfo, Device device) {
        if (statusInfo.getStatus() == StatusInfo.Status.InitialisationComplete) {
            // The StatusInfo object holds the different transaction statuses like reading card, pin entry, etc.
            // Let's launch a payment

        }
    }

    @Override
    public void endOfTransaction(TransactionResult transactionResult, Device device) {
        // The TransactionResult object holds details about the transaction as well as the receipts
        // Useful information can be accessed through this object like the transaction ID, the amount, etc.
    }

    @Override
    public void transactionResultReady(TransactionResult transactionResult, Device device) {
        // Pending TransactionResult objects will be received through this event if the EndOfTransaction
        // event was not delivered during the transaction, for example because of a network issue.
        // Here you are also going to receive a TransactionResult object after making a query to getTransactionStatus
    }

    public void disconnect() {
        this.api.disconnect();
        //This disconnects the connection
    }
}