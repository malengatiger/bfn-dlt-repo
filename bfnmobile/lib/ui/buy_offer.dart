import 'dart:ui';

import 'package:bfnlibrary/data/account.dart';
import 'package:bfnlibrary/data/invoice_offer.dart';
import 'package:bfnlibrary/util/functions.dart';
import 'package:bfnlibrary/util/net.dart';
import 'package:bfnlibrary/util/prefs.dart';
import 'package:bfnlibrary/util/snack.dart';
import 'package:flutter/material.dart';

import '../bloc.dart';

class BuyOffer extends StatefulWidget {
  final InvoiceOffer offer;

  BuyOffer(this.offer);

  @override
  _BuyOfferState createState() => _BuyOfferState();
}

class _BuyOfferState extends State<BuyOffer> implements SnackBarListener {
  AccountInfo account;
  var _key = GlobalKey<ScaffoldState>();
  @override
  initState() {
    super.initState();
    _checkIfValidBuyer();
  }

  _checkIfValidBuyer() async {
    account = await Prefs.getAccount();
    //check what action to take ...
    if (account.identifier == widget.offer.supplier.identifier) {
      //just observing ...
      _exit();
    }
    if (account.identifier == widget.offer.customer.identifier) {
      //just observing ...
      _exit();
    }
    if (account.identifier == widget.offer.investor.identifier) {
      //a buyInvoice is possible ...
    }
    setState(() {});
  }

  String message;
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      key: _key,
      appBar: AppBar(
        title: Text('Buy Invoice Offer'),
        bottom: PreferredSize(
            child: Column(
              children: <Widget>[
                Center(
                  child: Padding(
                    padding: const EdgeInsets.only(left: 28.0),
                    child: account == null
                        ? Container()
                        : NameBadge(
                            account: account,
                            elevation: 0.5,
                            nodeStyle: Styles.whiteSmall,
                          ),
                  ),
                ),
                SizedBox(
                  height: 20,
                ),
                StreamBuilder<String>(
                    stream: bfnBloc.fcmStream,
                    initialData: 'No network message yet',
                    builder: (context, snapshot) {
                      if (snapshot.hasData) {
                        debugPrint(
                            ' 😡  😡  😡  😡  FCM message arrived on Stream: ${snapshot.data}  😡  😡  😡  😡 ');
                        message = snapshot.data;
                      }
                      return Text(
                        '$message',
                        style: Styles.whiteSmall,
                      );
                    }),
                SizedBox(height: 20),
              ],
            ),
            preferredSize: Size.fromHeight(140)),
      ),
      backgroundColor: Colors.brown[100],
      body: Padding(
        padding: const EdgeInsets.all(8.0),
        child: ListView(
          children: <Widget>[
            SizedBox(
              height: 20,
            ),
            Card(
              elevation: 2,
              child: Padding(
                padding: const EdgeInsets.all(8.0),
                child: Padding(
                  padding: const EdgeInsets.only(left: 20.0),
                  child: Column(
                    children: <Widget>[
                      SizedBox(
                        height: 8,
                      ),
                      Row(
                        children: <Widget>[
                          Text(
                            'Customer',
                            style: Styles.greyLabelSmall,
                          ),
                          SizedBox(
                            width: 4,
                          ),
                          Text(
                            widget.offer.customer.name,
                            style: Styles.blackBoldMedium,
                          ),
                        ],
                      ),
                      SizedBox(
                        height: 8,
                      ),
                      Row(
                        children: <Widget>[
                          Text(
                            'Supplier',
                            style: Styles.greyLabelSmall,
                          ),
                          SizedBox(
                            width: 4,
                          ),
                          Text(
                            widget.offer.supplier.name,
                            style: Styles.blackBoldMedium,
                          ),
                        ],
                      ),
                      SizedBox(
                        height: 8,
                      ),
                      Row(
                        children: <Widget>[
                          Text(
                            'Buyer',
                            style: Styles.greyLabelSmall,
                          ),
                          SizedBox(
                            width: 4,
                          ),
                          Text(
                            widget.offer.investor.name,
                            style: Styles.blueBoldSmall,
                          ),
                        ],
                      ),
                      SizedBox(
                        height: 24,
                      ),
                      Row(
                        children: <Widget>[
                          Text(
                            'Invoice Amount',
                            style: Styles.greyLabelSmall,
                          ),
                          SizedBox(
                            width: 4,
                          ),
                          Text(
                            getCurrency(widget.offer.originalAmount, context),
                            style: Styles.blackBoldSmall,
                          ),
                        ],
                      ),
                      SizedBox(
                        height: 28,
                      ),
                      Row(
                        children: <Widget>[
                          Text(
                            'Discount',
                            style: Styles.greyLabelSmall,
                          ),
                          SizedBox(
                            width: 24,
                          ),
                          Text(
                            '${getCurrency(widget.offer.discount, context)} %',
                            style: Styles.tealBoldLarge,
                          ),
                        ],
                      ),
                      SizedBox(
                        height: 24,
                      ),
                      Row(
                        children: <Widget>[
                          Text(
                            'Amount',
                            style: Styles.greyLabelSmall,
                          ),
                          SizedBox(
                            width: 16,
                          ),
                          Text(
                            getCurrency(widget.offer.offerAmount, context),
                            style: Styles.blackBoldLarge,
                          ),
                        ],
                      ),
                      SizedBox(
                        height: 8,
                      ),
                      Row(
                        children: <Widget>[
                          Text(
                            'Offered',
                            style: Styles.greyLabelSmall,
                          ),
                          SizedBox(
                            width: 4,
                          ),
                          Text(
                            getFormattedDateShortWithTime(
                                widget.offer.offerDate, context),
                            style: Styles.blueSmall,
                          ),
                        ],
                      ),
                      SizedBox(
                        height: 40,
                      ),
                      RaisedButton(
                        onPressed: _confirm,
                        elevation: 8,
                        color: Theme.of(context).primaryColor,
                        child: Padding(
                          padding: const EdgeInsets.all(16.0),
                          child: Text(
                            "Submit Buy Instruction",
                            style: Styles.whiteSmall,
                          ),
                        ),
                      ),
                      SizedBox(
                        height: 20,
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  String getCurrency(double amt, BuildContext context) {
    return getFormattedAmount(amt.toString(), context);
  }

  _exit() {
    Navigator.pop(context);
  }

  void _confirm() {
    showDialog(
        context: context,
        builder: (context) {
          return AlertDialog(
            title: Text('Purchase Confirmation'),
            content: Container(
              height: 180,
              child: Column(
                children: <Widget>[
                  Text(
                      'Please confirm your purchase instruction. You will be notified when the transaction is sucessful. Sometimes someone else may have bought this offer just before you did'),
                  SizedBox(
                    height: 20,
                  ),
                  Text(
                    getCurrency(widget.offer.offerAmount, context),
                    style: Styles.blackBoldLarge,
                  ),
                ],
              ),
            ),
            actions: <Widget>[
              FlatButton(
                onPressed: () {
                  Navigator.pop(context);
                },
                child: Text(
                  'Cancel',
                  style: Styles.blueBoldSmall,
                ),
              ),
              FlatButton(
                onPressed: () {
                  Navigator.pop(context);
                  _submitOffer();
                },
                child: Text(
                  'CONFIRM',
                  style: Styles.pinkBoldMedium,
                ),
              ),
            ],
          );
        });
  }

  _submitOffer() async {
    AppSnackbar.showSnackbarWithProgressIndicator(
        scaffoldKey: _key,
        message: 'Submitting Buy Instruction',
        textColor: Colors.white,
        backgroundColor: Theme.of(context).primaryColor);
    try {
      var result = await Net.buyInvoiceOffer(widget.offer.invoiceId);
      print('👽 👽 👽 👽 👽  result of buyOffer call: $result');

      AppSnackbar.showSnackbarWithAction(
          scaffoldKey: _key,
          message: 'Submission completed OK',
          textColor: Colors.white,
          action: 1,
          actionLabel: 'OK',
          listener: this,
          backgroundColor: Colors.teal[900]);
    } catch (e) {
      AppSnackbar.showErrorSnackbar(
          scaffoldKey: _key, message: 'Submission failed', actionLabel: '');
    }
  }

  @override
  onActionPressed(int action) {
    switch (action) {
      case 1:
        Navigator.pop(context);
    }
    return null;
  }
}

class NameBadge extends StatelessWidget {
  final AccountInfo account;
  final Color textColor, backgroundColor;
  final double elevation;
  final TextStyle nameStyle, nodeStyle;

  NameBadge(
      {@required this.account,
      this.textColor,
      this.nameStyle,
      this.nodeStyle,
      this.backgroundColor,
      this.elevation});

  @override
  Widget build(BuildContext context) {
    StringBuffer loc = StringBuffer();
    var mList = account.host.split(',');
    mList.forEach((m) {
      var xList = m.split('=');
      loc.write(xList.elementAt(1) + ' ');
    });
    var node = loc.toString();
    return Padding(
      padding: const EdgeInsets.only(left: 20, right: 20, top: 12, bottom: 8),
      child: Column(
        children: <Widget>[
          Text(
            account.name,
            style: nameStyle == null ? Styles.blackBoldSmall : nameStyle,
          ),
          SizedBox(
            height: 4,
          ),
          Text(
            node,
            style: nodeStyle == null ? Styles.blackSmall : nodeStyle,
          ),
          SizedBox(
            height: 4,
          ),
        ],
      ),
    );
  }
}
