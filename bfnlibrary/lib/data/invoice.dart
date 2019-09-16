class Invoice {
  String invoiceId;
  String invoiceNumber;
  String description;
  String supplierId, customerId, dateRegistered;
  double amount, valueAddedTax, totalAmount;

  Invoice(
      this.invoiceId,
      this.invoiceNumber,
      this.description,
      this.supplierId,
      this.customerId,
      this.dateRegistered,
      this.amount,
      this.valueAddedTax,
      this.totalAmount);

  Invoice.fromJson(Map data) {
    this.invoiceId = data['invoiceId'];
    this.invoiceNumber = data['invoiceNumber'];
    this.description = data['description'];
    this.supplierId = data['supplierId'];
    this.customerId = data['customerId'];
    this.dateRegistered = data['dateRegistered'];
    if (data['amount'] is int) {
      this.amount = data['amount'] * 1.00;
    }
    if (data['amount'] is double) {
      this.amount = data['amount'];
    }
    if (data['valueAddedTax'] is int) {
      this.valueAddedTax = data['valueAddedTax'] * 1.00;
    }
    if (data['valueAddedTax'] is double) {
      this.valueAddedTax = data['valueAddedTax'];
    }
    if (data['totalAmount'] is int) {
      this.totalAmount = data['totalAmount'] * 1.00;
    }
    if (data['totalAmount'] is double) {
      this.totalAmount = data['totalAmount'];
    }
  }

  Map<String, dynamic> toJson() => <String, dynamic>{
        'invoiceId': invoiceId,
        'invoiceNumber': invoiceNumber,
        'description': description,
        'supplierId': supplierId,
        'amount': amount,
        'valueAddedTax': valueAddedTax,
        'totalAmount': totalAmount,
        'dateRegistered': dateRegistered,
        'customerId': customerId,
      };
}
