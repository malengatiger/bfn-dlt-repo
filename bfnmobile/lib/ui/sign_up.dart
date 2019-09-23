import 'file:///Users/aubs/WORK/CORDA/bfn-dlt-repo/bfnmobile/lib/bloc.dart';
import 'package:bfnlibrary/data/account.dart';
import 'package:bfnlibrary/util/net.dart';
import 'package:bfnlibrary/util/slide_right.dart';
import 'package:bfnlibrary/util/snack.dart';
import 'package:bfnlibrary/data/fb_user.dart';
import 'package:bfnmobile/prefs.dart';
import 'package:flutter/material.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'dashboard.dart';

class SignUp extends StatefulWidget {
  @override
  _SignUpState createState() => _SignUpState();
}

class _SignUpState extends State<SignUp> {
  final _key = GlobalKey<ScaffoldState>();
  final _nameKey = GlobalKey<FormState>();
  final _emailKey = GlobalKey<FormState>();
  final _cellKey = GlobalKey<FormState>();
  final _passKey = GlobalKey<FormState>();
  final _formKey = GlobalKey<FormState>();

  String name, email, cellphone, password;
  FirebaseAuth auth = FirebaseAuth.instance;
  @override
  Widget build(BuildContext context) {
    return WillPopScope(
      child: Scaffold(
        key: _key,
        appBar: AppBar(
          leading: Icon(Icons.people),
          title: Text('BFN SignUp'),
          bottom: PreferredSize(
              child: Column(
                children: <Widget>[
                  Text(
                    "Business Finance Network",
                    style: TextStyle(
                        color: Colors.white,
                        fontWeight: FontWeight.w900,
                        fontSize: 24),
                  ),
                  SizedBox(
                    height: 24,
                  )
                ],
              ),
              preferredSize: Size.fromHeight(60)),
        ),
        backgroundColor: Colors.brown[100],
        body: ListView(
          children: <Widget>[
            Padding(
              padding: const EdgeInsets.all(8.0),
              child: Card(
                  elevation: 4,
                  child: Padding(
                    padding: const EdgeInsets.all(16.0),
                    child: Form(
                      key: _formKey,
                      child: Column(
                        children: <Widget>[
                          Text(
                            "Account Details",
                            style: TextStyle(
                                fontSize: 28, fontWeight: FontWeight.w900),
                          ),
                          SizedBox(
                            height: 20,
                          ),
                          TextFormField(
                            key: _nameKey,
                            decoration: InputDecoration(
                                icon: Icon(Icons.person),
                                hintText: "Enter Name",
                                labelText: "Name"),
                            keyboardType: TextInputType.text,
                            // The validator receives the text that the user has entered.
                            validator: (value) {
//                              if (value.isEmpty) {
//                                return 'Please enter name';
//                              }
                              name = value;
                              return null;
                            },
                          ),
                          TextFormField(
                            key: _emailKey,
                            decoration: InputDecoration(
                                icon: Icon(Icons.email),
                                hintText: "Enter Email Address",
                                labelText: "Email"),
                            keyboardType: TextInputType.emailAddress,
                            // The validator receives the text that the user has entered.
                            validator: (value) {
                              if (value.isEmpty) {
                                return 'Please enter email address';
                              }
                              email = value;
                              return null;
                            },
                          ),
                          TextFormField(
                            key: _cellKey,
                            decoration: InputDecoration(
                                icon: Icon(Icons.phone),
                                hintText: "Enter Cellphone Number",
                                labelText: " Cellphone Number"),
                            keyboardType: TextInputType.phone,
                            // The validator receives the text that the user has entered.
                            validator: (value) {
//                              if (value.isEmpty) {
//                                return 'Please enter cellphone number';
//                              }
                              cellphone = value;
                              return null;
                            },
                          ),
                          TextFormField(
                            key: _passKey,
                            decoration: InputDecoration(
                                icon: Icon(Icons.vpn_key),
                                hintText: "Enter Password",
                                labelText: "Password"),
                            keyboardType: TextInputType.visiblePassword,
                            // The validator receives the text that the user has entered.
                            validator: (value) {
                              if (value.isEmpty) {
                                return 'Please enter password';
                              }
                              password = value;
                              return null;
                            },
                          ),
                          SizedBox(
                            height: 40,
                          ),
                          RaisedButton(
                            color: Colors.indigo,
                            elevation: 8,
                            child: Padding(
                              padding: const EdgeInsets.all(20.0),
                              child: Text(
                                "Create Account",
                                style: TextStyle(color: Colors.white),
                              ),
                            ),
                            onPressed: _validate,
                          )
                        ],
                      ),
                    ),
                  )),
            ),
          ],
        ),
      ), onWillPop: () => doNothing(),
    );
  }

  _validate() async {
    if (_formKey.currentState.validate()) {
      print("🍎 🍊 ready to rumble $name $email $cellphone $password");
      UserRecord userRecord;
      try {
        userRecord = await Net.getUser(email);
      } catch (e) {
        print(e);
      }
      AccountInfo accountInfo;
      try {
        if (userRecord != null) {
          AuthResult authResult = await auth.signInWithEmailAndPassword(email: email, password: password);
          if (authResult.user != null) {
            //get account info - using uid
            try {
              accountInfo = await Net.getAccount(authResult.user.uid);
            } catch (e) {
              print(e);
            }
          } else {
            accountInfo = await Net.startAccountRegistrationFlow(
                name, email, password, cellphone);
          }
          //sign IN
        } else {
          accountInfo = await Net.startAccountRegistrationFlow(
              name, email, password, cellphone);
        }

        print('🍎 🍊 🍎 🍊 🍎 🍊 acct found or created: ${accountInfo.toJson()} 🍎 🍊 🍎 🍊 🍎 🍊 ');
        await Prefs.saveAccount(accountInfo);
        var result = await bfnBloc.signIn(email, password);
        print('Signed in to Firebase: ${result.toString()}');
        Navigator.push(context, SlideRightRoute(
          widget: Dashboard(),
        ));
      } catch (e) {
        print(e);
        _error("Account registration failed");
      }
    }
  }

  _error(String msg) {
    AppSnackbar.showErrorSnackbar(scaffoldKey: _key, message: msg, actionLabel: "Err");
  }

  Future<bool> doNothing() async {
    return false;
  }
}
