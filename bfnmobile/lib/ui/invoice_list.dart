import 'package:bfnlibrary/data/invoice_offer.dart';
import 'file:///Users/aubs/WORK/CORDA/bfn-dlt-repo/bfnmobile/lib/bloc.dart';
import 'package:bfnlibrary/util/net.dart';
import 'package:flutter/material.dart';

class InvoiceList extends StatefulWidget {
  @override
  _InvoiceListState createState() => _InvoiceListState();
}

class _InvoiceListState extends State<InvoiceList> {
  var invoiceOffers = List<InvoiceOffer>();

  @override
  void initState() {
    super.initState();
    _getInvoices();
  }

  _getInvoices() async {
    invoiceOffers = await bfnBloc.getInvoiceOffers();
    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      children: <Widget>[
        Text('Open Offers'),
        ListView.builder(
            itemCount: invoiceOffers.length,
            itemBuilder: (BuildContext context, int index) {
              return Card(
                child: ListTile(
                  leading: Icon(Icons.open_with),
                  title: Text(
                      invoiceOffers.elementAt(index).offerAmount.toString()),
                  subtitle: Text(invoiceOffers.elementAt(index).invoiceId),
                ),
              );
            }),
      ],
    );
  }
}
