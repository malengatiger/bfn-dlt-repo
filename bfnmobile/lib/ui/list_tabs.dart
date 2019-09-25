import 'package:bfnlibrary/data/invoice.dart';
import 'package:bfnlibrary/data/invoice_offer.dart';
import 'package:bfnlibrary/util/functions.dart';
import 'package:bfnmobile/bloc.dart';
import 'package:flutter/material.dart';

class InvoicesPage extends StatefulWidget {
  @override
  _InvoicesPageState createState() => _InvoicesPageState();
}

class _InvoicesPageState extends State<InvoicesPage> {
  List<InvoiceOffer> offers = List();
  List<Invoice> invoices = List();

  @override
  void initState() {
    super.initState();
    _getInvoicesAndOffers();
  }

  _getInvoicesAndOffers() async {
    await bfnBloc.getMyAccount();
    offers =
        await bfnBloc.getInvoiceOffers(accountId: bfnBloc.account.identifier);
    invoices = await bfnBloc.getInvoices(accountId: bfnBloc.account.identifier);
    setState(() {});
    offers.forEach((o) {
      print(o.toJson());
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      home: DefaultTabController(
        length: 3,
        child: Scaffold(
          appBar: AppBar(
            backgroundColor: Colors.orange[600],
            leading: IconButton(
              icon: Icon(Icons.arrow_back),
              onPressed: () {
                Navigator.pop(context);
              },
            ),
            bottom: TabBar(
              tabs: [
                Tab(
                  icon: Icon(Icons.list),
                  text: 'Offers',
                ),
                Tab(
                  icon: Icon(Icons.apps),
                  text: 'Invoices',
                ),
                Tab(
                  icon: Icon(Icons.add_circle),
                  text: 'Create',
                ),
              ],
            ),
            title: Text('Invoices & Offers'),
          ),
          body: TabBarView(
            children: [
              OfferList(offers),
              InvoiceList(invoices),
              Card(
                child: CreateMenu(),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class OfferList extends StatelessWidget {
  final List<InvoiceOffer> offers;

  OfferList(this.offers);

  @override
  Widget build(BuildContext context) {
    return Stack(
      children: <Widget>[
        ListView.builder(
            itemCount: offers.length,
            itemBuilder: (context, index) {
              var color = Colors.pink[700];
              var offer = offers.elementAt(index);
//              if (bfnBloc.account.identifier == offer.supplier.) {
//                color = Colors.black;
//              }
              return Padding(
                padding: const EdgeInsets.only(top: 8.0, left: 20, right: 20),
                child: Card(
                  elevation: 4,
                  child: ListTile(
                    leading: Icon(
                      Icons.apps,
                      color: getRandomColor(),
                    ),
                    title: Text(
                      getCurrency(offers.elementAt(index).offerAmount, context),
                      style: TextStyle(
                          color: color,
                          fontWeight: FontWeight.w900,
                          fontSize: 20),
                    ),
                    subtitle: Column(
                      children: <Widget>[
                        Text(offers.elementAt(index).invoiceId),
                        Text(offers.elementAt(index).investor.name),
                      ],
                    ),
                  ),
                ),
              );
            }),
        Positioned(
          top: 20,
          right: 0,
          child: Container(
            height: 60,
            width: 80,
            child: Card(
              elevation: 20,
              color: Colors.yellow,
              child: Center(
                child: Text(
                  '${offers.length}',
                  style: Styles.pinkBoldMedium,
                ),
              ),
            ),
          ),
        ),
      ],
    );
  }

  String getCurrency(double amt, BuildContext context) {
    return getFormattedAmount(amt.toString(), context);
  }
}

class CreateMenu extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Card(
      child: Center(
        child: Column(
          children: <Widget>[
            SizedBox(
              height: 40,
            ),
            Padding(
              padding: const EdgeInsets.all(20.0),
              child: Text(
                  'Every invoice recorded on the Network should be from a customer who is already part of the Network. To create an invoice you must select your customer from the Accounts list'),
            ),
            SizedBox(
              height: 20,
            ),
            RaisedButton(
              color: Colors.pink,
              elevation: 4,
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Text(
                  'Create Invoice',
                  style: Styles.whiteSmall,
                ),
              ),
              onPressed: _createInvoicePressed,
            ),
            SizedBox(
              height: 20,
            ),
            Padding(
              padding: const EdgeInsets.all(20.0),
              child: Text(
                  'Every invoice recorded on the Network should be from a customer who is already part of the Network. To create an invoice you must select your customer from the Accounts list'),
            ),
            SizedBox(
              height: 20,
            ),
            RaisedButton(
              color: Colors.indigo,
              elevation: 4,
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Text(
                  'Create Offer',
                  style: Styles.whiteSmall,
                ),
              ),
              onPressed: _createInvoiceOfferPressed,
            ),
          ],
        ),
      ),
    );
  }

  void _createInvoicePressed() {}

  void _createInvoiceOfferPressed() {}
}

class InvoiceList extends StatelessWidget {
  final List<Invoice> invoices;

  InvoiceList(this.invoices);

  @override
  Widget build(BuildContext context) {
    return Stack(
      children: <Widget>[
        ListView.builder(
            itemCount: invoices.length,
            itemBuilder: (context, index) {
              var color = Colors.pink[700];
              var invoice = invoices.elementAt(index);
              if (bfnBloc.account.identifier == invoice.supplierId) {
                color = Colors.black;
              }
              return Padding(
                padding: const EdgeInsets.only(top: 8.0, left: 20, right: 20),
                child: Card(
                  elevation: 4,
                  child: ListTile(
                    leading: Icon(
                      Icons.account_balance,
                      color: Colors.black,
                    ),
                    title: Text(
                      getCurrency(invoice.totalAmount, context),
                      style: TextStyle(
                          color: color,
                          fontWeight: FontWeight.w900,
                          fontSize: 20),
                    ),
                    subtitle: Text(invoice.dateRegistered),
                  ),
                ),
              );
            }),
        Positioned(
          top: 20,
          right: 0,
          child: Container(
            height: 60,
            width: 80,
            child: Card(
              elevation: 20,
              color: Colors.teal[200],
              child: Center(
                child: Text(
                  '${invoices.length}',
                  style: Styles.blackBoldMedium,
                ),
              ),
            ),
          ),
        ),
      ],
    );
  }

  String getCurrency(double amt, BuildContext context) {
    return getFormattedAmount(amt.toString(), context);
  }
}
