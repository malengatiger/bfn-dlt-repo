import 'dart:ui';

import 'package:bfnlibrary/data/account.dart';
import 'package:bfnlibrary/data/invoice_offer.dart';
import 'package:bfnlibrary/util/functions.dart';
import 'package:bfnlibrary/util/prefs.dart';
import 'package:flutter/material.dart';

class BuyOffer extends StatefulWidget {
  final InvoiceOffer offer;

  BuyOffer(this.offer);

  @override
  _BuyOfferState createState() => _BuyOfferState();
}

class _BuyOfferState extends State<BuyOffer> {
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
                  height: 8,
                ),
              ],
            ),
            preferredSize: Size.fromHeight(60)),
      ),
    );
  }

  _exit() {
    Navigator.pop(context);
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
    return Card(
      elevation: elevation == null ? 0 : elevation,
      color: backgroundColor == null
          ? Theme.of(context).primaryColor
          : backgroundColor,
      child: Padding(
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
              account.host,
              style: nodeStyle == null ? Styles.blackSmall : nodeStyle,
            ),
            SizedBox(
              height: 4,
            ),
          ],
        ),
      ),
    );
  }
}
